# Data Model: Base Price Retrieval

## Source Product (FakeStoreAPI)
Represented as a JSON response from `GET https://fakestoreapi.com/products/{id}`.

| Field | Type | Example | Description |
| --- | --- | --- | --- |
| `id` | Integer | `1` | Unique product identifier |
| `title` | String | `"Backpack"` | Product title |
| `price` | Number | `109.95` | Numeric price value (Source of Base Price) |
| `description` | String | `"..."` | Product description |
| `category` | String | `"..."` | Product category |
| `image` | String (URI) | `"..."` | Image URL |
| `rating` | Object | `{...}` | Customer ratings |

## Target Base Price Response (Internal)
Exposed via `GET /api/v1/products/{productId}/base-price`.

| Field | Type | Example | Mapping Source | Validation |
| --- | --- | --- | --- | --- |
| `productId` | Integer | `1` | `$.id` (Source Product) | Required, Positive Integer |
| `basePrice` | Number | `109.95` | `$.price` (Source Product) | Required, Numeric |
| `currency` | String | `"USD"` | Hardcoded "USD" | Mandatory |

## Mapping Rules
- **Request**: `{productId}` path parameter is passed through to FakeStoreAPI as `{id}`.
- **Response**:
    - `$.id` → `$.productId`
    - `$.price` → `$.basePrice`
    - Static "USD" → `$.currency`

## Error Model (Problem Details / RFC 9457)
| Field | Type | Example | Description |
| --- | --- | --- | --- |
| `type` | String (URI) | `"about:blank"` | Error type identifier |
| `title` | String | `"Not Found"` | Short error title |
| `status` | Integer | `404` | HTTP status code |
| `detail` | String | `"Product not found"` | Detailed error message |
| `instance` | String (URI) | `"/api/v1/products/99/base-price"` | Request path |
