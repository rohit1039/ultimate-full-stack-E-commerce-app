## Author - Rohit Parida 🙋‍♂️

## Category Service - Ultimate E-commerce Application
- API version: v1.0.0

## Documentation for API Endpoints

All URIs are relative to *http://localhost:8081/categories*

Class | Method | HTTP request | Description
------------ | ------------- | ------------- | -------------
*CategoryServiceApi* | [**createCategory**](docs/CategoryServiceApi.md#createCategory) | **POST** /add | Create Category API
*CategoryServiceApi* | [**downloadFile**](docs/CategoryServiceApi.md#downloadFile) | **GET** /download-file/{image-name} | Download image of category by file name
*CategoryServiceApi* | [**exportToExcel**](docs/CategoryServiceApi.md#exportToExcel) | **GET** /export/excel | Export categories data in Excel
*CategoryServiceApi* | [**exportToPdf**](docs/CategoryServiceApi.md#exportToPdf) | **GET** /export/pdf | Export categories data in Pdf
*CategoryServiceApi* | [**getAllCategories**](docs/CategoryServiceApi.md#getAllCategories) | **GET** /all | Get all managed categories
*CategoryServiceApi* | [**getCategoryById**](docs/CategoryServiceApi.md#getCategoryById) | **GET** /get/{categoryId} | Get category by Id
*CategoryServiceApi* | [**updateCategory**](docs/CategoryServiceApi.md#updateCategory) | **PUT** /update/{categoryId} | Update Category
*CategoryServiceApi* | [**updateEnabled**](docs/CategoryServiceApi.md#updateEnabled) | **PATCH** /{categoryId}/update-enabled | Update Category Enable/Disable
*CategoryServiceApi* | [**uploadFile**](docs/CategoryServiceApi.md#uploadFile) | **POST** /upload-file/{categoryId} | Upload category&#x27;s image

## Documentation for Models

- [CategoryRequestDTO](docs/CategoryRequestDTO.md)

## Documentation for Authorization

Authentication schemes defined for the API:
### JWT_token

- **Type**: API key
- **API key parameter name**: Authorization
- **Location**: HTTP header