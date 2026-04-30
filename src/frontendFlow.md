# Just Eat Backend API Documentation

**Base URL:** `http://localhost:8081/api`

**Authentication:** JWT Bearer Token (add to header: `Authorization: Bearer <token>`)

---

## Table of Contents
1. [Authentication](#1-authentication)
2. [Password Reset](#2-password-reset)
3. [User Profile](#3-user-profile)
4. [User Preferences](#4-user-preferences)
5. [Restaurants](#5-restaurants)
6. [Menu Items](#6-menu-items)
7. [Popular Items](#7-popular-items)
8. [Cart](#8-cart)
9. [Orders](#9-orders)
10. [Enums & Constants](#10-enums--constants)
11. [Error Responses](#11-error-responses)

---

## 1. Authentication

### 1.1 Register Customer
**POST** `/auth/register`

**Access:** Public

**Request:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123"
}
```

**Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

### 1.2 Register Restaurant Owner
**POST** `/auth/owner/register`

**Access:** Public

**Request:**
```json
{
  "name": "Restaurant Owner",
  "email": "owner@example.com",
  "password": "password123"
}
```

**Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

### 1.3 Login
**POST** `/auth/login`

**Access:** Public

**Request:**
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

**Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

### 1.4 Logout
**POST** `/auth/logout`

**Access:** Authenticated

**Headers:**
```
Authorization: Bearer <token>
```

**Response (200):**
```
"Logged out successfully."
```

---

## 2. Password Reset

### 2.1 Forgot Password (Request Reset)
**POST** `/auth/password/forgot`

**Access:** Public

**Request:**
```json
{
  "email": "john@example.com"
}
```

**Response (200):**
```json
{
  "message": "If an account exists with this email, a password reset link has been sent."
}
```

---

### 2.2 Validate Reset Token
**GET** `/auth/password/validate-token?token={token}`

**Access:** Public

**Response (200):**
```json
{
  "valid": true,
  "message": "Token is valid."
}
```

---

### 2.3 Reset Password
**POST** `/auth/password/reset`

**Access:** Public

**Request:**
```json
{
  "token": "reset-token-from-email",
  "newPassword": "newPassword123"
}
```

**Response (200):**
```json
{
  "message": "Password has been reset successfully. You can now log in with your new password."
}
```

---

## 3. User Profile

### 3.1 Get Profile
**GET** `/user/me`

**Access:** Authenticated

**Response (200):**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "role": "CUSTOMER"
}
```

---

### 3.2 Update Profile
**PUT** `/user/me`

**Access:** Authenticated

**Request:**
```json
{
  "name": "John Updated",
  "password": "newPassword123"
}
```
> Note: Both fields are optional. Only provided fields will be updated.

**Response (200):**
```json
{
  "id": 1,
  "name": "John Updated",
  "email": "john@example.com",
  "role": "CUSTOMER"
}
```

---

## 4. User Preferences

### 4.1 Get Valid Options (Cuisines & Dietary Restrictions)
**GET** `/user/preferences/options`

**Access:** CUSTOMER

**Response (200):**
```json
{
  "cuisines": [
    "ITALIAN", "CHINESE", "INDIAN", "MEXICAN", "JAPANESE", "THAI", 
    "FRENCH", "AMERICAN", "MEDITERRANEAN", "KOREAN", "VIETNAMESE", 
    "GREEK", "SPANISH", "MIDDLE_EASTERN", "CARIBBEAN", "TURKISH", 
    "LEBANESE", "BRAZILIAN", "PERUVIAN", "ETHIOPIAN", "MOROCCAN", 
    "GERMAN", "BRITISH", "IRISH", "RUSSIAN", "POLISH", "INDONESIAN", 
    "MALAYSIAN", "FILIPINO", "SINGAPOREAN", "AFRICAN", "AUSTRALIAN", 
    "CAJUN", "HAWAIIAN", "SOUL_FOOD", "SOUTHERN", "TEX_MEX", "FUSION"
  ],
  "dietaryRestrictions": [
    "VEGETARIAN", "VEGAN", "GLUTEN_FREE", "DAIRY_FREE", "NUT_FREE", 
    "HALAL", "KOSHER", "PESCATARIAN", "KETO", "LOW_CARB", "LOW_SODIUM", 
    "LOW_FAT", "DIABETIC_FRIENDLY", "PALEO", "WHOLE30", "EGG_FREE", 
    "SOY_FREE", "SHELLFISH_FREE", "LACTOSE_INTOLERANT", "ORGANIC", 
    "RAW_FOOD", "FODMAP", "SUGAR_FREE"
  ]
}
```

---

### 4.2 Get User Preferences
**GET** `/user/preferences`

**Access:** CUSTOMER

**Response (200):**
```json
{
  "userId": 1,
  "favouriteRestaurantIds": [1, 2, 5],
  "favouriteCuisines": ["ITALIAN", "INDIAN"],
  "dietaryRestrictions": ["VEGETARIAN", "GLUTEN_FREE"]
}
```

---

### 4.3 Save/Update All Preferences
**PUT** `/user/preferences`

**Access:** CUSTOMER

**Request:**
```json
{
  "favouriteRestaurantIds": [1, 2, 5],
  "favouriteCuisines": ["ITALIAN", "INDIAN", "THAI"],
  "dietaryRestrictions": ["VEGETARIAN"]
}
```

**Response (200):**
```json
{
  "userId": 1,
  "favouriteRestaurantIds": [1, 2, 5],
  "favouriteCuisines": ["ITALIAN", "INDIAN", "THAI"],
  "dietaryRestrictions": ["VEGETARIAN"]
}
```

---

### 4.4 Add Favourite Restaurant
**POST** `/user/preferences/restaurants/{restaurantId}`

**Access:** CUSTOMER

**Response (200):**
```json
{
  "message": "Restaurant added to favourites."
}
```

---

### 4.5 Remove Favourite Restaurant
**DELETE** `/user/preferences/restaurants/{restaurantId}`

**Access:** CUSTOMER

**Response (200):**
```json
{
  "message": "Restaurant removed from favourites."
}
```

---

### 4.6 Add Favourite Cuisine
**POST** `/user/preferences/cuisines/{cuisine}`

**Access:** CUSTOMER

**Example:** `POST /user/preferences/cuisines/ITALIAN`

**Response (200):**
```json
{
  "message": "Cuisine added to favourites."
}
```

---

### 4.7 Remove Favourite Cuisine
**DELETE** `/user/preferences/cuisines/{cuisine}`

**Access:** CUSTOMER

**Response (200):**
```json
{
  "message": "Cuisine removed from favourites."
}
```

---

### 4.8 Add Dietary Restriction
**POST** `/user/preferences/dietary/{restriction}`

**Access:** CUSTOMER

**Example:** `POST /user/preferences/dietary/VEGAN`

**Response (200):**
```json
{
  "message": "Dietary restriction added."
}
```

---

### 4.9 Remove Dietary Restriction
**DELETE** `/user/preferences/dietary/{restriction}`

**Access:** CUSTOMER

**Response (200):**
```json
{
  "message": "Dietary restriction removed."
}
```

---

## 5. Restaurants

### 5.1 Get All Restaurants
**GET** `/restaurants`

**Access:** Public (Authenticated)

**Response (200):**
```json
[
  {
    "id": 1,
    "name": "Pizza Palace",
    "description": "Best Italian pizzas in town",
    "cuisine": "Italian",
    "location": "123 Main Street",
    "rating": 4.5,
    "ownerName": "John Owner",
    "ownerEmail": "owner@pizza.com"
  }
]
```

---

### 5.2 Search Restaurants
**GET** `/restaurants/search`

**Access:** Public (Authenticated)

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| name | string | No | Search by restaurant name |
| location | string | No | Search by location |
| cuisine | string | No | Search by cuisine type |

**Example:** `GET /restaurants/search?cuisine=Italian&location=Main`

**Response (200):**
```json
[
  {
    "id": 1,
    "name": "Pizza Palace",
    "description": "Best Italian pizzas in town",
    "cuisine": "Italian",
    "location": "123 Main Street",
    "rating": 4.5,
    "ownerName": "John Owner",
    "ownerEmail": "owner@pizza.com"
  }
]
```

---

### 5.3 Create Restaurant
**POST** `/restaurants`

**Access:** OWNER

**Request:**
```json
{
  "name": "Pizza Palace",
  "description": "Best Italian pizzas in town",
  "location": "123 Main Street",
  "cuisine": "Italian"
}
```

**Response (201):**
```json
{
  "id": 1,
  "name": "Pizza Palace",
  "description": "Best Italian pizzas in town",
  "cuisine": "Italian",
  "location": "123 Main Street",
  "rating": null,
  "ownerName": "John Owner",
  "ownerEmail": "owner@pizza.com"
}
```

---

### 5.4 Update Restaurant
**PUT** `/restaurants/{id}`

**Access:** OWNER (own restaurant only)

**Request:**
```json
{
  "name": "Pizza Palace Updated",
  "description": "Updated description",
  "location": "456 New Street",
  "cuisine": "Italian"
}
```

**Response (200):**
```json
{
  "id": 1,
  "name": "Pizza Palace Updated",
  "description": "Updated description",
  "cuisine": "Italian",
  "location": "456 New Street",
  "rating": 4.5,
  "ownerName": "John Owner",
  "ownerEmail": "owner@pizza.com"
}
```

---

### 5.5 Delete Restaurant
**DELETE** `/restaurants/{id}`

**Access:** OWNER (own restaurant only)

**Response (200):**
```
"Restaurant deleted successfully."
```

---

## 6. Menu Items

### 6.1 Get Menu by Restaurant
**GET** `/menu/restaurant/{restaurantId}`

**Access:** Authenticated

**Response (200):**
```json
[
  {
    "id": 1,
    "name": "Margherita Pizza",
    "description": "Classic tomato and mozzarella",
    "price": 12.99,
    "isAvailable": true,
    "isSpecial": false,
    "restaurantName": "Pizza Palace"
  }
]
```

---

### 6.2 Get All Specials
**GET** `/menu/specials`

**Access:** Authenticated

**Response (200):**
```json
[
  {
    "id": 3,
    "name": "Chef's Special Pasta",
    "description": "Daily special pasta dish",
    "price": 15.99,
    "isAvailable": true,
    "isSpecial": true,
    "restaurantName": "Pizza Palace"
  }
]
```

---

### 6.3 Add Menu Item
**POST** `/menu`

**Access:** OWNER

**Request:**
```json
{
  "name": "Margherita Pizza",
  "description": "Classic tomato and mozzarella",
  "price": 12.99,
  "isAvailable": true,
  "isSpecial": false,
  "restaurantId": 1
}
```

**Response (201):**
```json
{
  "id": 1,
  "name": "Margherita Pizza",
  "description": "Classic tomato and mozzarella",
  "price": 12.99,
  "isAvailable": true,
  "isSpecial": false,
  "restaurantName": "Pizza Palace"
}
```

---

### 6.4 Update Menu Item
**PUT** `/menu/{id}`

**Access:** OWNER (own restaurant's items only)

**Request:**
```json
{
  "name": "Margherita Pizza Deluxe",
  "description": "Updated description",
  "price": 14.99,
  "isAvailable": true,
  "isSpecial": true,
  "restaurantId": 1
}
```

**Response (200):**
```json
{
  "id": 1,
  "name": "Margherita Pizza Deluxe",
  "description": "Updated description",
  "price": 14.99,
  "isAvailable": true,
  "isSpecial": true,
  "restaurantName": "Pizza Palace"
}
```

---

### 6.5 Delete Menu Item
**DELETE** `/menu/{id}`

**Access:** OWNER (own restaurant's items only)

**Response (200):**
```
"Menu item deleted successfully."
```

---

## 7. Popular Items (Mostly Ordered)

### 7.1 Get Popular Items for Restaurant
**GET** `/restaurants/{restaurantId}/popular-items`

**Access:** Authenticated

**Response (200):**
```json
[
  {
    "id": 1,
    "name": "Margherita Pizza",
    "description": "Classic tomato and mozzarella",
    "price": 12.99,
    "orderCount": 150,
    "isPopular": true,
    "isAvailable": true,
    "restaurantId": 1,
    "restaurantName": "Pizza Palace"
  }
]
```

---

### 7.2 Get All Items Sorted by Order Count
**GET** `/restaurants/{restaurantId}/popular-items/all`

**Access:** OWNER

**Response (200):**
```json
[
  {
    "id": 1,
    "name": "Margherita Pizza",
    "description": "Classic tomato and mozzarella",
    "price": 12.99,
    "orderCount": 150,
    "isPopular": true,
    "isAvailable": true,
    "restaurantId": 1,
    "restaurantName": "Pizza Palace"
  },
  {
    "id": 2,
    "name": "Garlic Bread",
    "description": "Crispy garlic bread",
    "price": 5.99,
    "orderCount": 75,
    "isPopular": false,
    "isAvailable": true,
    "restaurantId": 1,
    "restaurantName": "Pizza Palace"
  }
]
```

---

### 7.3 Recalculate Popularity
**POST** `/restaurants/{restaurantId}/popular-items/recalculate`

**Access:** OWNER

**Response (200):**
```json
{
  "message": "Popularity recalculated successfully for restaurant 1"
}
```

---

## 8. Cart

### 8.1 Get Cart
**GET** `/cart`

**Access:** CUSTOMER

**Response (200):**
```json
{
  "cartId": 1,
  "userId": 1,
  "userEmail": "john@example.com",
  "items": [
    {
      "itemId": 1,
      "menuItemId": 5,
      "name": "Margherita Pizza",
      "price": 12.99,
      "quantity": 2,
      "totalPrice": 25.98
    }
  ],
  "totalCartPrice": 25.98
}
```

---

### 8.2 Add Item to Cart
**POST** `/cart`

**Access:** CUSTOMER

**Request:**
```json
{
  "menuItemId": 5,
  "quantity": 2
}
```

**Response (200):**
```json
{
  "cartId": 1,
  "userId": 1,
  "userEmail": "john@example.com",
  "items": [
    {
      "itemId": 1,
      "menuItemId": 5,
      "name": "Margherita Pizza",
      "price": 12.99,
      "quantity": 2,
      "totalPrice": 25.98
    }
  ],
  "totalCartPrice": 25.98
}
```

---

### 8.3 Update Item Quantity
**PUT** `/cart/{itemId}`

**Access:** CUSTOMER

**Request:**
```json
{
  "menuItemId": 5,
  "quantity": 3
}
```

**Response (200):**
```json
{
  "cartId": 1,
  "userId": 1,
  "userEmail": "john@example.com",
  "items": [
    {
      "itemId": 1,
      "menuItemId": 5,
      "name": "Margherita Pizza",
      "price": 12.99,
      "quantity": 3,
      "totalPrice": 38.97
    }
  ],
  "totalCartPrice": 38.97
}
```

---

### 8.4 Remove Item from Cart
**DELETE** `/cart/{itemId}`

**Access:** CUSTOMER

**Response (200):**
```json
{
  "cartId": 1,
  "userId": 1,
  "userEmail": "john@example.com",
  "items": [],
  "totalCartPrice": 0.0
}
```

---

### 8.5 Clear Cart
**DELETE** `/cart`

**Access:** CUSTOMER

**Response (200):**
```
"Cart cleared successfully."
```

---

## 9. Orders

### 9.1 Place Order
**POST** `/orders/place`

**Access:** CUSTOMER

> Note: Places order from current cart. Cart must have items from same restaurant.

**Response (201):**
```json
{
  "orderId": 1,
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "restaurantName": "Pizza Palace",
  "items": [
    {
      "id": 1,
      "name": "Margherita Pizza",
      "quantity": 2,
      "price": 12.99,
      "totalPrice": 25.98
    }
  ],
  "totalAmount": 25.98,
  "status": "PENDING",
  "createdAt": "2026-05-01T10:30:00"
}
```

---

### 9.2 Get My Orders (Customer)
**GET** `/orders/my`

**Access:** CUSTOMER

**Response (200):**
```json
[
  {
    "orderId": 1,
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "restaurantName": "Pizza Palace",
    "items": [
      {
        "id": 1,
        "name": "Margherita Pizza",
        "quantity": 2,
        "price": 12.99,
        "totalPrice": 25.98
      }
    ],
    "totalAmount": 25.98,
    "status": "PREPARING",
    "createdAt": "2026-05-01T10:30:00"
  }
]
```

---

### 9.3 Get Order by ID
**GET** `/orders/{id}`

**Access:** CUSTOMER (own orders) / OWNER (their restaurant orders)

**Response (200):**
```json
{
  "orderId": 1,
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "restaurantName": "Pizza Palace",
  "items": [
    {
      "id": 1,
      "name": "Margherita Pizza",
      "quantity": 2,
      "price": 12.99,
      "totalPrice": 25.98
    }
  ],
  "totalAmount": 25.98,
  "status": "PREPARING",
  "createdAt": "2026-05-01T10:30:00"
}
```

---

### 9.4 Get Restaurant Orders (Owner)
**GET** `/orders/restaurant`

**Access:** OWNER

**Response (200):**
```json
[
  {
    "orderId": 1,
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "restaurantName": "Pizza Palace",
    "items": [
      {
        "id": 1,
        "name": "Margherita Pizza",
        "quantity": 2,
        "price": 12.99,
        "totalPrice": 25.98
      }
    ],
    "totalAmount": 25.98,
    "status": "PENDING",
    "createdAt": "2026-05-01T10:30:00"
  }
]
```

---

### 9.5 Update Order Status
**PUT** `/orders/{id}/status`

**Access:** OWNER

**Request:**
```json
{
  "status": "PREPARING"
}
```

> Status Flow: `PENDING` → `PREPARING` → `READY` → `COMPLETED`

**Response (200):**
```json
{
  "orderId": 1,
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "restaurantName": "Pizza Palace",
  "items": [
    {
      "id": 1,
      "name": "Margherita Pizza",
      "quantity": 2,
      "price": 12.99,
      "totalPrice": 25.98
    }
  ],
  "totalAmount": 25.98,
  "status": "PREPARING",
  "createdAt": "2026-05-01T10:30:00"
}
```

---

## 10. Enums & Constants

### Roles
```
CUSTOMER - Regular user who can order food
OWNER    - Restaurant owner who can manage restaurants and menu
```

### Order Status
```
PENDING    - Order placed, waiting for restaurant to accept
PREPARING  - Restaurant is preparing the order
READY      - Order is ready for pickup/delivery
COMPLETED  - Order has been delivered/picked up
```

### Cuisine Types
```
ITALIAN, CHINESE, INDIAN, MEXICAN, JAPANESE, THAI, FRENCH, AMERICAN,
MEDITERRANEAN, KOREAN, VIETNAMESE, GREEK, SPANISH, MIDDLE_EASTERN,
CARIBBEAN, TURKISH, LEBANESE, BRAZILIAN, PERUVIAN, ETHIOPIAN, MOROCCAN,
GERMAN, BRITISH, IRISH, RUSSIAN, POLISH, INDONESIAN, MALAYSIAN, FILIPINO,
SINGAPOREAN, AFRICAN, AUSTRALIAN, CAJUN, HAWAIIAN, SOUL_FOOD, SOUTHERN,
TEX_MEX, FUSION
```

### Dietary Restrictions
```
VEGETARIAN, VEGAN, GLUTEN_FREE, DAIRY_FREE, NUT_FREE, HALAL, KOSHER,
PESCATARIAN, KETO, LOW_CARB, LOW_SODIUM, LOW_FAT, DIABETIC_FRIENDLY,
PALEO, WHOLE30, EGG_FREE, SOY_FREE, SHELLFISH_FREE, LACTOSE_INTOLERANT,
ORGANIC, RAW_FOOD, FODMAP, SUGAR_FREE
```

---

## 11. Error Responses

### Validation Error (400)
```json
{
  "name": "Name is required",
  "email": "Invalid email format",
  "password": "Password must be at least 6 characters"
}
```

### Bad Request (400)
```json
{
  "error": "Cart is empty. Please add items to cart before placing an order."
}
```

### Unauthorized (401)
```json
{
  "error": "Invalid or expired token."
}
```

### Forbidden (403)
```json
{
  "error": "You are not authorized to update this order."
}
```

### Not Found (404)
```json
{
  "error": "Restaurant not found with id: 99"
}
```

### Conflict/Business Logic Error (400)
```json
{
  "error": "Invalid status transition: Cannot change from PENDING to COMPLETED"
}
```

---

## Frontend Pages Mapping

| Page | Endpoints Used |
|------|----------------|
| **Login** | `POST /auth/login` |
| **Register (Customer)** | `POST /auth/register` |
| **Register (Owner)** | `POST /auth/owner/register` |
| **Forgot Password** | `POST /auth/password/forgot` |
| **Reset Password** | `GET /auth/password/validate-token`, `POST /auth/password/reset` |
| **Profile** | `GET /user/me`, `PUT /user/me` |
| **Preferences** | `GET /user/preferences`, `PUT /user/preferences`, `GET /user/preferences/options` |
| **Home/Restaurant List** | `GET /restaurants`, `GET /restaurants/search` |
| **Restaurant Details** | `GET /menu/restaurant/{id}`, `GET /restaurants/{id}/popular-items` |
| **Cart** | `GET /cart`, `POST /cart`, `PUT /cart/{id}`, `DELETE /cart/{id}`, `DELETE /cart` |
| **Checkout** | `POST /orders/place` |
| **Order History (Customer)** | `GET /orders/my`, `GET /orders/{id}` |
| **Dashboard (Owner)** | `GET /orders/restaurant`, `PUT /orders/{id}/status` |
| **Menu Management (Owner)** | `POST /menu`, `PUT /menu/{id}`, `DELETE /menu/{id}` |
| **Restaurant Management (Owner)** | `POST /restaurants`, `PUT /restaurants/{id}`, `DELETE /restaurants/{id}` |
| **Popular Items (Owner)** | `GET /restaurants/{id}/popular-items/all`, `POST /restaurants/{id}/popular-items/recalculate` |

---

## Authentication Header Example

```javascript
// Store token after login
localStorage.setItem('token', response.token);

// Use token in API calls
fetch('http://localhost:8081/api/user/me', {
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('token')}`,
    'Content-Type': 'application/json'
  }
});
```

