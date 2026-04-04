## ADDED Requirements

### Requirement: HTTP Error Status Codes

The system SHALL return appropriate HTTP status codes for all error responses. Status codes 400 and above indicate error conditions.

#### Scenario: Authentication failure returns 401
- **WHEN** an unauthenticated user accesses a protected resource
- **THEN** the system returns HTTP 401 Unauthorized with error response body

#### Scenario: Authorization failure returns 403
- **WHEN** an authenticated user accesses a resource without required permissions
- **THEN** the system returns HTTP 403 Forbidden with error response body

#### Scenario: Resource not found returns 404
- **WHEN** a requested resource does not exist
- **THEN** the system returns HTTP 404 Not Found with error response body

#### Scenario: Validation error returns 400
- **WHEN** request parameters fail validation
- **THEN** the system returns HTTP 400 Bad Request with validation error details

#### Scenario: Business logic error returns 400
- **WHEN** a business rule is violated (e.g., duplicate username)
- **THEN** the system returns HTTP 400 Bad Request with error message

#### Scenario: Internal server error returns 500
- **WHEN** an unexpected exception occurs
- **THEN** the system returns HTTP 500 Internal Server Error

---

### Requirement: Unified Error Response Structure

The system SHALL use a unified error response structure for all error responses.

#### Scenario: Error response contains required fields
- **WHEN** an error response is returned
- **THEN** the response body contains `timestamp`, `status`, `error`, and `message` fields

#### Scenario: Error response includes business code
- **WHEN** a business error occurs with a specific error code
- **THEN** the response body includes a `code` field with the business error code

#### Scenario: Validation errors include details
- **WHEN** a validation error occurs with multiple field errors
- **THEN** the response body includes a `details` map with field names and error messages

---

### Requirement: Global Exception Handler

The system SHALL provide a unified global exception handler that handles all exceptions and returns appropriate HTTP status codes.

#### Scenario: Security exceptions are handled
- **WHEN** `AuthenticationException` or `AccessDeniedException` is thrown
- **THEN** the handler returns appropriate 401 or 403 status code

#### Scenario: Business exceptions are handled
- **WHEN** `ServiceException` is thrown
- **THEN** the handler returns 400 status code with the exception message

#### Scenario: Custom HTTP status exceptions are handled
- **WHEN** `HttpStatusCodeException` is thrown with a specific status code
- **THEN** the handler returns the specified status code

#### Scenario: Unknown exceptions return 500
- **WHEN** an unhandled exception is thrown
- **THEN** the handler returns 500 status code and logs the exception

---

### Requirement: Frontend Error Response Handling

The frontend SHALL handle HTTP error status codes correctly in the response interceptor.

#### Scenario: 2xx responses are treated as success
- **WHEN** the server returns a 2xx status code
- **THEN** the frontend resolves the promise with response data

#### Scenario: 401 triggers login redirect
- **WHEN** the server returns 401 Unauthorized
- **THEN** the frontend redirects to the login page

#### Scenario: Error responses display error message
- **WHEN** the server returns an error status code (>=400)
- **THEN** the frontend displays the error message from response body
