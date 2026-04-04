## ADDED Requirements

### Requirement: Standard Validation Annotations

The system SHALL use Jakarta Bean Validation annotations for input validation on DTOs and entity classes.

#### Scenario: NotBlank validation
- **WHEN** a field annotated with `@NotBlank` receives a null or empty string
- **THEN** the system returns HTTP 400 with the field name and error message

#### Scenario: Size validation
- **WHEN** a field annotated with `@Size(max=50)` receives a string longer than 50 characters
- **THEN** the system returns HTTP 400 with the error message

#### Scenario: Pattern validation
- **WHEN** a field annotated with `@Pattern(regexp="...")` receives a value not matching the pattern
- **THEN** the system returns HTTP 400 with the error message

---

### Requirement: Controller Method Validation

The system SHALL validate all request parameters including path variables and request bodies.

#### Scenario: Request body validation
- **WHEN** a controller method parameter is annotated with `@Valid` or `@Validated`
- **THEN** the request body is validated before the method executes

#### Scenario: Path variable validation
- **WHEN** a path variable is annotated with validation constraints like `@Min(1)`
- **THEN** the path variable is validated before the method executes

#### Scenario: Method parameter validation
- **WHEN** a controller class is annotated with `@Validated`
- **AND** a method parameter has validation constraints
- **THEN** the parameter is validated before the method executes

---

### Requirement: Custom Mobile Validation

The system SHALL provide a `@Mobile` annotation for validating Chinese mobile phone numbers.

#### Scenario: Valid mobile number
- **WHEN** a field annotated with `@Mobile` receives a valid 11-digit Chinese mobile number (starting with 1)
- **THEN** validation passes

#### Scenario: Invalid mobile number
- **WHEN** a field annotated with `@Mobile` receives an invalid mobile number
- **THEN** the system returns HTTP 400 with message "手机号格式不正确"

#### Scenario: Null value handling
- **WHEN** a field annotated with `@Mobile` receives a null value
- **THEN** validation passes (use `@NotBlank` together for required fields)

---

### Requirement: Custom ID Card Validation

The system SHALL provide an `@IdCard` annotation for validating Chinese ID card numbers.

#### Scenario: Valid 18-digit ID card
- **WHEN** a field annotated with `@IdCard` receives a valid 18-digit ID card number
- **THEN** validation passes

#### Scenario: Valid 15-digit ID card (legacy)
- **WHEN** a field annotated with `@IdCard` receives a valid 15-digit ID card number
- **THEN** validation passes

#### Scenario: Invalid ID card
- **WHEN** a field annotated with `@IdCard` receives an invalid ID card number
- **THEN** the system returns HTTP 400 with message "身份证号格式不正确"

---

### Requirement: Custom Enum Value Validation

The system SHALL provide an `@EnumValue` annotation for validating that a value exists in a specified enum.

#### Scenario: Valid enum value
- **WHEN** a field annotated with `@EnumValue(enumClass=Status.class)` receives a valid enum value
- **THEN** validation passes

#### Scenario: Invalid enum value
- **WHEN** a field annotated with `@EnumValue(enumClass=Status.class)` receives a value not in the enum
- **THEN** the system returns HTTP 400 with message "值必须在枚举范围内"

---

### Requirement: Validation Exception Handling

The system SHALL handle all validation exceptions uniformly and return appropriate HTTP responses.

#### Scenario: BindException handling
- **WHEN** form data binding fails validation
- **THEN** the system returns HTTP 400 with field-level error details

#### Scenario: MethodArgumentNotValidException handling
- **WHEN** JSON request body fails validation
- **THEN** the system returns HTTP 400 with field-level error details

#### Scenario: ConstraintViolationException handling
- **WHEN** method-level validation fails (path variables, query parameters)
- **THEN** the system returns HTTP 400 with field-level error details
