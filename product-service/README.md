## Author - Rohit Parida 🙋‍♂️

## Documentation for API Endpoints

All URIs are relative to *http://172.20.10.2:8083/products*

Class | Method | HTTP request | Description
------------ | ------------- | ------------- | -------------
*ProductServiceApi* | [**addProduct**](docs/ProductServiceApi.md#addProduct) | **POST** /add/{categoryId} | Add a new product
*ProductServiceApi* | [**getAllProducts**](docs/ProductServiceApi.md#getAllProducts) | **GET** /get/all | Get all products
*ProductServiceApi* | [**getProductById**](docs/ProductServiceApi.md#getProductById) | **GET** /get/{productId} | Get product by Id
*ProductServiceApi* | [**getProductsByCategory**](docs/ProductServiceApi.md#getProductsByCategory) | **GET** /get/by-category/{categoryId} | Get products by category
*ProductServiceApi* | [**productsOnPurchase**](docs/ProductServiceApi.md#productsOnPurchase) | **PUT** /reduce-count/{productId} | Reduce product count
*ProductServiceApi* | [**updateProduct**](docs/ProductServiceApi.md#updateProduct) | **PUT** /update/{productId} | Update product by Id

## Documentation for Models

- [FilterProvider](docs/FilterProvider.md)
- [MappingJacksonValue](docs/MappingJacksonValue.md)
- [ProductRequestDTO](docs/ProductRequestDTO.md)
- [Size](docs/Size.md)

## Documentation for Authorization

Authentication schemes defined for the API:
### JWT_token

- **Type**: API key
- **API key parameter name**: Authorization
- **Location**: HTTP header