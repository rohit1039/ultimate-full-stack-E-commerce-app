package com.ecommerce.productservice.service;

import static com.ecommerce.productservice.config.RedisConfig.CACHE_NAME;
import static java.util.Objects.isNull;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import com.ecommerce.productservice.exception.DuplicateProductException;
import com.ecommerce.productservice.exception.ProductNotFoundException;
import com.ecommerce.productservice.exception.UnAuthorizedException;
import com.ecommerce.productservice.model.Product;
import com.ecommerce.productservice.model.Size;
import com.ecommerce.productservice.payload.request.OrderProductDTO;
import com.ecommerce.productservice.payload.request.ProductRequestDTO;
import com.ecommerce.productservice.payload.response.ProductResponseDTO;
import com.ecommerce.productservice.repository.ProductRepository;
import com.ecommerce.productservice.util.MongoSequenceGenerator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Implementation of the {@link ProductService} interface. This class provides the necessary methods
 * to manage products in the system, such as creating, retrieving, updating, and deleting products.
 * It also facilitates caching and database operations along with additional utility operations for
 * managing product counts.
 *
 * <p>Dependencies: - LOGGER: Logger for logging information related to product service operations.
 * - productRepository: Repository for database interactions with product records. - restTemplate:
 * Used for making HTTP requests. - mongoTemplate: Template for operations with MongoDB. -
 * redisTemplate: Template for operations with Redis cache. - modelMapper: Mapper tool to convert
 * between entity and DTO objects. - mongoSequenceGenerator: Utility for generating unique sequence
 * IDs in MongoDB.
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProductServiceImpl.class);

  private final ProductRepository productRepository;

  private final RestTemplate restTemplate;

  private final MongoTemplate mongoTemplate;

  private final RedisTemplate<String, ProductResponseDTO> redisTemplate;

  private final ModelMapper modelMapper;

  private final MongoSequenceGenerator mongoSequenceGenerator;

  /**
   * Saves a product to the database after performing validations and mapping the given product
   * data. If the product is valid and unique, it is persisted in the database, and cached.
   *
   * @param productRequest Details of the product to be saved, encapsulated in a ProductRequestDTO
   *     object.
   * @param categoryId The unique identifier of the category to which the product belongs.
   * @param username The username of the user performing the operation.
   * @param role The role of the user; must be "ROLE_ADMIN" to successfully save the product.
   * @return A ProductResponseDTO containing details of the saved product.
   * @throws UnAuthorizedException If the user's role is not "ROLE_ADMIN".
   * @throws DuplicateProductException If a product with the same name already exists.
   */
  @Override
  @Caching(
      evict = {
        @CacheEvict(value = CACHE_NAME, key = "productRequest.productName", allEntries = true)
      })
  public ProductResponseDTO saveProductToDB(
      ProductRequestDTO productRequest, Integer categoryId, String username, String role) {
    // make a GET request to the category service to get the category
    ResponseEntity<Object> categoryResponse =
        restTemplate.getForEntity(
            "http://category-service/categories/v1" + "/get/{categoryId}",
            Object.class,
            categoryId);
    // if the user's role is not admin, throw an exception
    if (!isNull(role) && role.equals("ROLE_USER")) {
      LOGGER.error("*** {} ***", "Role needs to be ADMIN to add a product");
      throw new UnAuthorizedException("Requires ROLE_ADMIN to add a product");
    }
    // check if the product is unique
    ProductResponseDTO productResponseDTO = null;
    if (productIsUnique(productRequest.getProductName())) {
      // map the product request DTO to a product object
      Product product = modelMapper.map(productRequest, Product.class);
      product.setCreatedAt(LocalDateTime.now());
      product.setShortDescription(product.getShortDescription());
      if (categoryResponse.getStatusCode().is2xxSuccessful()) {
        product.setProductCount(product.getProductCount());
        product.setProductColor(product.getProductColor().toLowerCase());
        product.setDiscountedPrice(product.getDiscountedPrice());
        product.setTotalPrice(product.getTotalPrice());
        product.setCategoryId(categoryId);
        product.setExtraProductImages(productRequest.getExtraProductImages());
        product.setProductMainImage(productRequest.getProductMainImage());
        product.setEnabled(true);
        product.setProductBrand(product.getProductBrand());
        product.setUsername(username);
        product.setInStock(product.isInStock());

        if (product.getProductSizes() != null) {
          product
              .getProductSizes()
              .forEach(
                  size -> {
                    if (size.getReservedQuantity() == null) {
                      size.setReservedQuantity(0);
                    }
                  });
        }
        // save the product to the database
        Product productToSaveInDB = productRepository.save(product);
        this.redisTemplate
            .opsForHash()
            .put(CACHE_NAME, productToSaveInDB.getProductId(), productToSaveInDB);
        // map the saved product to a product response DTO
        ProductResponseDTO productToDTO =
            modelMapper.map(productToSaveInDB, ProductResponseDTO.class);
        LOGGER.info("*** {} ***", "Product Saved Successfully");
        productToDTO.setShortDescription(productToDTO.getShortDescription());
        productResponseDTO = this.modelMapper.map(productToDTO, ProductResponseDTO.class);
      }
    } else {
      // if the product is not unique, throw a duplicate product exception
      throw new DuplicateProductException("Products cannot be duplicated");
    }
    // return the product response
    return productResponseDTO;
  }

  /**
   * Retrieves a product by its unique identifier. If the product is not found in the cache, it
   * fetches the product from the database. The product must be enabled to be returned. If no
   * product exists with the given identifier, a {@link ProductNotFoundException} is thrown.
   *
   * @param productId the unique identifier of the product to retrieve
   * @return a {@link ProductResponseDTO} containing the details of the product
   * @throws ProductNotFoundException if no enabled product is found with the specified ID
   */
  @Override
  @Cacheable(value = CACHE_NAME, key = "#productId")
  public ProductResponseDTO getProductById(Integer productId) {

    LOGGER.info(
        "*** Searching in database as product with Id: {} not found in cache ***", productId);
    Optional<Product> product = productRepository.findById(Long.valueOf(productId));
    if (product.isPresent() && product.get().isEnabled()) {
      // map the product to a product response DTO and return it
      return modelMapper.map(product.get(), ProductResponseDTO.class);
    }
    throw new ProductNotFoundException("Product not found with ID: " + productId);
  }

  /**
   * Retrieves a paginated list of products that belong to the specified category.
   *
   * @param categoryId the unique identifier of the category for which products need to be fetched
   * @param pageNumber the page number to retrieve (1-based index)
   * @param pageSize the number of products per page
   * @param searchKey an optional search query to filter products by name or other attributes
   * @param role the role of the user making the request, which may influence visible products
   * @return a paginated list of products matching the specified category and search criteria
   */
  @Override
  @Cacheable(
      value = CACHE_NAME,
      key = "{#categoryId, #pageNumber, #pageSize, #searchKey, #role}",
      unless = "#result.getContent().size()==0")
  public Page<ProductResponseDTO> findProductsByCategory(
      Integer categoryId, int pageNumber, int pageSize, String searchKey, String role) {

    this.restTemplate.getForEntity(
        "http://category-service/categories/v1/get/{categoryId}", Object.class, categoryId);
    // create a pageable object with the given page number and page size
    Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
    return getPageOfFilteredProducts(categoryId, pageable, searchKey, role);
  }

  /**
   * Retrieves a paginated list of products based on the provided page number, page size, and
   * optional search key. The result is cached unless the resulting page is empty.
   *
   * @param pageNumber the number of the page to retrieve, starting from 1
   * @param pageSize the size of the page to retrieve (number of items per page)
   * @param searchKey an optional search keyword to filter products; can be null or empty
   * @return a {@code Page} containing the list of {@code ProductResponseDTO} matching the criteria,
   *     or an empty page if no products match
   */
  @Override
  @Cacheable(
      value = CACHE_NAME,
      key = "{#pageNumber, #pageSize, #searchKey}",
      unless = "#result.getContent" + "().size()==0")
  public Page<ProductResponseDTO> getAllProducts(int pageNumber, int pageSize, String searchKey) {
    // create a pageable object with the given page number and page size
    Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
    return getPageOfFilteredProducts(0, pageable, searchKey, null);
  }

  /**
   * Updates an existing product by its ID. This method allows updating product details such as
   * color, images, sizes, name, brand, descriptions, discount, price, and other related
   * information. Note that only users with the 'ADMIN' role are permitted to perform this action.
   *
   * @param productId The unique identifier of the product to be updated.
   * @param productRequestDTO An object containing the updated product details.
   * @param username The username of the person performing the update operation.
   * @param role The role of the user attempting to update the product; must be 'ADMIN'.
   * @return A ProductResponseDTO object containing the updated product details.
   * @throws UnAuthorizedException if the user is not authorized to update the product.
   */
  @Override
  @Caching(evict = {@CacheEvict(value = CACHE_NAME, key = "#productId", allEntries = true)})
  public ProductResponseDTO updateProductById(
      Integer productId, ProductRequestDTO productRequestDTO, String username, String role) {
    // if the user's role is not admin, throw an exception
    if (!isNull(role) && role.equals("ROLE_USER")) {
      LOGGER.error("*** {} ***", "Role needs to be ADMIN to update a product");
      throw new UnAuthorizedException("Requires ROLE_ADMIN to update a product");
    }
    // save updatedProduct to database
    ProductResponseDTO productInDB = this.getProductById(productId);
    Product product = this.modelMapper.map(productInDB, Product.class);
    // set values to existing product from payload
    product.setProductColor(productRequestDTO.getProductColor());
    product.setUpdatedAt(LocalDateTime.now());
    product.setExtraProductImages(productRequestDTO.getExtraProductImages());
    product.setProductMainImage(productRequestDTO.getProductMainImage());
    product.setProductSizes(productRequestDTO.getProductSizes());
    product.setProductName(productRequestDTO.getProductName());
    product.setProductBrand(productRequestDTO.getProductBrand());
    product.setCreatedAt(product.getCreatedAt());
    product.setShortDescription(productRequestDTO.getShortDescription());
    product.setLongDescription(productRequestDTO.getLongDescription());
    product.setDiscountPercent(productRequestDTO.getDiscountPercent());
    product.setProductPrice(productRequestDTO.getProductPrice());
    product.setProductCount(product.getProductCount());
    product.setDiscountedPrice(product.getDiscountedPrice());
    product.setTotalPrice(product.getTotalPrice());
    product.setUsername(username);
    // find and replace the product
    Optional<Product> findAndReplaceProduct =
        this.mongoTemplate
            .update(Product.class)
            .matching(query(where("_id").is(productId)))
            .replaceWith(product)
            .withOptions(FindAndReplaceOptions.options().upsert().returnNew())
            .as(Product.class)
            .findAndReplace();
    findAndReplaceProduct.ifPresent(
        value -> this.redisTemplate.opsForHash().put(CACHE_NAME, value.getProductId(), value));
    ProductResponseDTO responseDTO;
    responseDTO = modelMapper.map(findAndReplaceProduct.get(), ProductResponseDTO.class);
    return responseDTO;
  }

  /**
   * Deletes a product by its ID. The product is marked as disabled in the database. Only users with
   * the "ROLE_ADMIN" role are authorized to perform this operation.
   *
   * @param productId the ID of the product to be deleted
   * @param role the role of the user attempting the delete operation, must be "ROLE_ADMIN" to
   *     authorize deletion
   * @throws UnAuthorizedException if the user does not have the "ROLE_ADMIN" privilege
   */
  @Override
  @Caching(evict = {@CacheEvict(value = CACHE_NAME, key = "#productId", allEntries = true)})
  public void deleteProductById(Integer productId, String role) {
    // if the user's role is not admin, throw an exception
    if (!isNull(role) && role.equals("ROLE_USER")) {
      LOGGER.error("*** {} ***", "Role needs to be ADMIN to delete a product");
      throw new UnAuthorizedException("Requires ROLE_ADMIN to delete a product");
    }
    Optional<Product> findAndReplaceProduct =
        this.productRepository.findById(Long.valueOf(productId));
    if (findAndReplaceProduct.isPresent()) {
      Product product = findAndReplaceProduct.get();
      product.setEnabled(false);
      this.mongoTemplate
          .update(Product.class)
          .matching(query(where("_id").is(productId)))
          .replaceWith(product)
          .withOptions(FindAndReplaceOptions.options().upsert().returnNew())
          .as(Product.class)
          .findAndReplace();
    }
    LOGGER.info("Product with Id: {} deleted successfully", productId);
  }

  /**
   * Reduces product count by updating the reserved quantities for each product size in the
   * inventory. This method first deduplicates the list of products, aggregates the quantities for
   * the same product IDs and sizes, and then updates the reserved quantities in the database. It
   * ensures stock availability and throws an exception if requested quantities exceed available
   * stock.
   *
   * @param products a list of {@code OrderProductDTO} objects representing the ordered products
   *     with their respective IDs, sizes, and quantities. The list may contain duplicates which
   *     will be aggregated within the method.
   */
  @Override
  @Caching(evict = {@CacheEvict(value = CACHE_NAME, key = "#productId", allEntries = true)})
  public void reduceProductCount(List<OrderProductDTO> products) {

    Map<String, OrderProductDTO> uniqueMap = new HashMap<>();
    for (OrderProductDTO item : products) {
      String key = item.getProductId() + "-" + item.getSize();
      uniqueMap.merge(
          key,
          item,
          (existing, dup) -> {
            existing.setQuantity(existing.getQuantity() + dup.getQuantity());
            return existing;
          });
    }
    List<OrderProductDTO> deduplicatedList = new ArrayList<>(uniqueMap.values());

    Map<Integer, Product> productMap =
        deduplicatedList.stream()
            .map(
                item -> {
                  ProductResponseDTO dto = getProductById(item.getProductId());
                  return modelMapper.map(dto, Product.class);
                })
            .collect(
                Collectors.toMap(
                    Product::getProductId, Function.identity(), (existing, duplicate) -> existing));

    for (OrderProductDTO prod : deduplicatedList) {
      Product product = productMap.get(prod.getProductId());

      // Find matching size
      Size matchedSize =
          product.getProductSizes().stream()
              .filter(size -> size.getName().equals(prod.getSize()))
              .findFirst()
              .orElseThrow(
                  () ->
                      new RuntimeException(
                          "❌ Size not found for product ID: " + prod.getProductId()));

      int available = matchedSize.getQuantity() - matchedSize.getReservedQuantity();
      if (available < prod.getQuantity()) {
        LOGGER.error(
            "Product ID: {}, size: {} is out of stock!",
            prod.getProductId(),
            prod.getSize());
        throw new RuntimeException("Insufficient stock for size: " + prod.getSize());
      }

      // Update reserved quantity safely
      Query reserveQuery =
          new Query(
              Criteria.where("_id")
                  .is(prod.getProductId())
                  .and("product_sizes.name")
                  .is(prod.getSize()));
      Update reserveUpdate =
          new Update().inc("product_sizes.$.reservedQuantity", prod.getQuantity());

      mongoTemplate.findAndModify(reserveQuery, reserveUpdate, Product.class);

      LOGGER.info(
          "✅ Reserved Product ID: {}, Size: {}, Quantity: {}",
          prod.getProductId(),
          prod.getSize(),
          prod.getQuantity());
    }
  }

  /**
   * Retrieves a list of products eligible for export. Products are filtered to include only those
   * that are enabled. The filtered products are then mapped to a list of ProductResponseDTO
   * objects.
   *
   * @return a list of ProductResponseDTO representing the enabled products eligible for export
   */
  @Override
  public List<ProductResponseDTO> findProductsToExport() {

    return this.productRepository.findAll().stream()
        .filter(Product::isEnabled)
        .map(product -> this.modelMapper.map(product, ProductResponseDTO.class))
        .toList();
  }

  /**
   * Checks if a product with the given name is unique by verifying if no enabled products exist
   * with the specified name in the repository.
   *
   * @param productName the name of the product to be checked for uniqueness
   * @return true if no enabled products with the given name exist, false otherwise
   */
  private boolean productIsUnique(String productName) {
    // get the products with the given product name
    List<Product> products =
        productRepository.findByProductName(productName).stream()
            .filter(Product::isEnabled)
            .toList();
    // check if any products were found
    return products.isEmpty();
  }

  /**
   * Retrieves a pageable list of filtered products based on the provided category, search key, and
   * role.
   *
   * @param categoryId the ID of the category to filter products by. If 0, retrieves products across
   *     all categories.
   * @param pageable the Pageable object containing pagination information such as page number and
   *     size.
   * @param searchKey the search string used to filter products based on their attributes such as
   *     product name, brand, description, or color.
   * @param role the role of the user (e.g., "ROLE_ADMIN"), which determines if non-enabled products
   *     are included in the results.
   * @return a Page object containing a list of ProductResponseDTO objects that match the filtering
   *     criteria, along with pagination metadata.
   */
  public Page<ProductResponseDTO> getPageOfFilteredProducts(
      Integer categoryId, Pageable pageable, String searchKey, String role) {

    Page<ProductResponseDTO> page;
    // create a query object
    Query query = new Query();
    // find the products using the mongo template and the query object
    if (!isNull(role) && role.equals("ROLE_ADMIN")) {
      query.with(pageable);
    } else {
      query.with(pageable).addCriteria(where("is_enabled").is(true));
    }
    query.addCriteria(
        new Criteria()
            .orOperator(
                where("product_name").regex(searchKey, "i"),
                where("product_brand").regex(searchKey, "i"),
                where("short_desc").regex(searchKey, "i"),
                where("long_desc").regex(searchKey, "i"),
                where("product_color").regex(searchKey, "i")));
    List<Product> products = mongoTemplate.find(query, Product.class);
    List<ProductResponseDTO> productByCategory;
    if (categoryId != 0) {
      if (!isNull(role) && role.equals("ROLE_ADMIN")) {
        LOGGER.info("findProductsByCategory::Populating database response in cache");
        // filter the products based on the category id
        productByCategory =
            products.stream()
                .filter(product -> product.getCategoryId().equals(categoryId))
                .map(product -> modelMapper.map(product, ProductResponseDTO.class))
                .toList();
      } else {
        LOGGER.info("findProductsByCategory::Populating database response in cache");
        // filter the products based on the category id
        productByCategory =
            products.stream()
                .filter(
                    product -> product.getCategoryId().equals(categoryId) && product.isEnabled())
                .map(product -> modelMapper.map(product, ProductResponseDTO.class))
                .toList();
      }
      // create a page with the products and the total number of products
      page =
          PageableExecutionUtils.getPage(
                  productByCategory,
                  pageable,
                  () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Product.class))
              .map(u -> modelMapper.map(u, ProductResponseDTO.class));
    } else {
      if (!isNull(role) && role.equals("ROLE_ADMIN")) {
        LOGGER.info("getAllProducts::Populating database response in cache");
        page =
            PageableExecutionUtils.getPage(
                    products,
                    pageable,
                    () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Product.class))
                .map(u -> modelMapper.map(u, ProductResponseDTO.class));
      } else {
        LOGGER.info("getAllProducts::Populating database response in cache");
        page =
            PageableExecutionUtils.getPage(
                    products.stream().filter(Product::isEnabled).toList(),
                    pageable,
                    () -> mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Product.class))
                .map(u -> modelMapper.map(u, ProductResponseDTO.class));
      }
    }
    return new PageImpl<>(page.getContent(), pageable, page.getTotalElements());
  }

  /**
   * Releases reserved quantities of products based on the provided list of products. The method
   * updates the reserve counts for each product and size combination by decreasing the reserved
   * quantity.
   *
   * @param products a list of {@code OrderProductDTO} objects containing product IDs, sizes, and
   *     quantities to be released.
   */
  public void releaseReservedProductCount(List<OrderProductDTO> products) {

    products.forEach(
        prod -> {
          ProductResponseDTO productInDB = this.getProductById(prod.getProductId());
          Product product = this.modelMapper.map(productInDB, Product.class);

          Size matchedSize =
              product.getProductSizes().stream()
                  .filter(size -> size.getName().equals(prod.getSize()))
                  .findFirst()
                  .orElseThrow(
                      () ->
                          new RuntimeException(
                              "Size not found for product: " + prod.getProductId()));

          if (matchedSize.getReservedQuantity() < prod.getQuantity()) {
            LOGGER.warn(
                "⚠️ Trying to release more than reserved for product: {}, size: {}",
                prod.getProductId(),
                prod.getSize());
            return;
          }

          Query query = new Query();
          query.addCriteria(
              where("_id")
                  .is(prod.getProductId())
                  .and("product_sizes.name")
                  .is(prod.getSize()));

          Update update = new Update().inc("product_sizes.$.reservedQuantity", -prod.getQuantity());

          this.mongoTemplate.findAndModify(query, update, Product.class);

          LOGGER.info(
              "↩️ Released reserved product Id: {}, size: {}, quantity: {}",
              prod.getProductId(),
              prod.getSize(),
              prod.getQuantity());
        });
  }

  /**
   * Confirms the product count for the given list of products by checking reserved quantities,
   * updating stock levels, and ensuring consistency for each specified product size.
   *
   * @param products a list of {@code OrderProductDTO} objects, where each object contains details
   *     about the product including its ID, size, and the quantity to confirm. The method processes
   *     this list to check stock availability and then updates the product's reserved and available
   *     quantities.
   */
  @Override
  public void confirmProductCount(List<OrderProductDTO> products) {

    Map<String, OrderProductDTO> mergedProducts = new HashMap<>();
    for (OrderProductDTO prod : products) {
      String key = prod.getProductId() + "-" + prod.getSize();
      mergedProducts.merge(
          key,
          prod,
          (existing, dup) -> {
            existing.setQuantity(existing.getQuantity() + dup.getQuantity());
            return existing;
          });
    }
    List<OrderProductDTO> deduplicatedList = new ArrayList<>(mergedProducts.values());

    for (OrderProductDTO prod : deduplicatedList) {

      ProductResponseDTO productDTO = getProductById(prod.getProductId());
      Product product = modelMapper.map(productDTO, Product.class);

      System.out.println(product.toString());

      Size matchedSize =
          product.getProductSizes().stream()
              .filter(size -> size.getName().equals(prod.getSize()))
              .findFirst()
              .orElseThrow(
                  () ->
                      new RuntimeException(
                          "❌ Size not found for product ID: " + prod.getProductId()));

      if (matchedSize.getReservedQuantity() < prod.getQuantity()) {
        throw new RuntimeException(
            "Cannot confirm more than reserved for product: " + prod.getProductId());
      }

      Query stockUpdateQuery =
          new Query(
              where("_id")
                  .is(prod.getProductId())
                  .and("product_sizes.name")
                  .is(prod.getSize()));

      Update stockUpdate =
          new Update()
              .inc("product_sizes.$.quantity", -prod.getQuantity())
              .inc("product_sizes.$.reservedQuantity", -prod.getQuantity());

      mongoTemplate.findAndModify(stockUpdateQuery, stockUpdate, Product.class);

      Product updatedProduct = modelMapper.map(getProductById(prod.getProductId()), Product.class);

      int totalCount = updatedProduct.getProductSizes().stream().mapToInt(Size::getQuantity).sum();

      Query countUpdateQuery = new Query(where("_id").is(prod.getProductId()));
      Update countUpdate = new Update().set("product_count", totalCount);
      mongoTemplate.findAndModify(countUpdateQuery, countUpdate, Product.class);

      LOGGER.info(
          "✅ Confirmed stock - Product ID: {}, Size: {}, Quantity: {}",
          prod.getProductId(),
          prod.getSize(),
          prod.getQuantity());
    }
  }
}
