## MODIFIED Requirements

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

#### Scenario: Constraint violation exception is handled
- **WHEN** `ConstraintViolationException` is thrown (method-level validation failure)
- **THEN** the handler returns 400 status code with field-level error details in the response body
