## Author - Rohit Parida 🙋‍♂️

## User Service - Ultimate E-commerce Application

- ##### API version: v1.0.0

## Documentation for API Endpoints

All URIs are relative to *http://localhost:8081/users*

| Class                      | Method                                                                | HTTP request                                | Description                                                             |
----------------------------|-----------------------------------------------------------------------|---------------------------------------------|-------------------------------------------------------------------------
| *AuthenticationServiceApi* | [**forgotPassword**](docs/AuthenticationServiceApi.md#forgotPassword) | **PATCH** /v1/auth/forgot/password          | Reset password for user, and use the updated password for future access |
| *AuthenticationServiceApi* | [**registerUser**](docs/AuthenticationServiceApi.md#registerUser)     | **POST** /v1/auth/register                  | Register a new user to be managed by the admins                         |
| *AuthenticationServiceApi* | [**userLogin**](docs/AuthenticationServiceApi.md#userLogin)           | **POST** /v1/auth/login                     | Login to get a token for authentication                                 |
| *UserServiceApi*           | [**downloadFile**](docs/UserServiceApi.md#downloadFile)               | **GET** /v1/download-file/{avatarName}      | Download image of user by file code                                     |
| *UserServiceApi*           | [**exportToExcel**](docs/UserServiceApi.md#exportToExcel)             | **GET** /v1/export/excel                    | Export user&#x27;s data in Excel                                        |
| *UserServiceApi*           | [**exportToPdf**](docs/UserServiceApi.md#exportToPdf)                 | **GET** /v1/export/pdf                      | Export user&#x27;s data in Pdf                                          |
| *UserServiceApi*           | [**getAllUsers**](docs/UserServiceApi.md#getAllUsers)                 | **GET** /v1/all                             | Get all users managed by admins                                         |
| *UserServiceApi*           | [**getCurrentUserRole**](docs/UserServiceApi.md#getCurrentUserRole)   | **GET** /v1/current/role                    | Get current loggedIn user&#x27;s role accessible by all users           |
| *UserServiceApi*           | [**getUserByEmail**](docs/UserServiceApi.md#getUserByEmail)           | **GET** /v1/get/{username}                  | Get user by username managed by admins                                  |
| *UserServiceApi*           | [**updateUserRole**](docs/UserServiceApi.md#updateUserRole)           | **PATCH** /v1/role/update/{username}/{role} | Update user by user&#x27;s role                                         |
| *UserServiceApi*           | [**uploadFile**](docs/UserServiceApi.md#uploadFile)                   | **POST** /v1/upload-file/{username}         | Upload user&#x27;s display picture                                      |
| *UserServiceApi*           | [**userDelete**](docs/UserServiceApi.md#userDelete)                   | **DELETE** /v1/delete/{username}            | Delete user by username                                                 |
| *UserServiceApi*           | [**userUpdate**](docs/UserServiceApi.md#userUpdate)                   | **PUT** /v1/update                          | Update user by username managed by all users                            |

## Documentation for Models

- [ExceptionInResponse](docs/ExceptionInResponse.md)
- [ForgotPasswordDTO](docs/ForgotPasswordDTO.md)
- [ForgotPasswordResponse](docs/ForgotPasswordResponse.md)
- [LoginDTO](docs/LoginDTO.md)
- [UpdateUserDTO](docs/UpdateUserDTO.md)
- [UploadfileUsernameBody](docs/UploadfileUsernameBody.md)
- [UserApiResponse](docs/UserApiResponse.md)
- [UserApiResponseEmbedded](docs/UserApiResponseEmbedded.md)
- [UserDTO](docs/UserDTO.md)

## Documentation for Authorization

Authentication schemes defined for the API:

### JWT_token

- **Type**: API key
- **API key parameter name**: Authorization
- **Location**: HTTP header


