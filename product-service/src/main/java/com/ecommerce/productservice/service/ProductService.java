package com.ecommerce.productservice.service;

import com.ecommerce.productservice.payload.request.OrderProductDTO;
import com.ecommerce.productservice.payload.request.ProductRequestDTO;
import com.ecommerce.productservice.payload.response.ProductResponseDTO;
import java.util.List;
import org.springframework.data.domain.Page;

public interface ProductService {

  ProductResponseDTO saveProductToDB(
      ProductRequestDTO productRequestDTO, Integer categoryId, String username, String role)
      throws Exception;

  ProductResponseDTO getProductById(Integer productId);

  Page<ProductResponseDTO> findProductsByCategory(
      Integer categoryId, int pageNumber, int pageSize, String searchKey, String role);

  Page<ProductResponseDTO> getAllProducts(int pageNumber, int pageSize, String searchKey);

  ProductResponseDTO updateProductById(
      Integer productId, ProductRequestDTO productRequestDTO, String username, String role)
      throws Exception;

  void deleteProductById(Integer productId, String role) throws Exception;

  void reduceProductCount(List<OrderProductDTO> products);

  void releaseReservedProductCount(List<OrderProductDTO> products);

  void confirmProductCount(List<OrderProductDTO> products);

  List<ProductResponseDTO> findProductsToExport();
}
