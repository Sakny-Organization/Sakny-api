# Listing Module

This module manages room and apartment listings for the Sakny roommate-finding platform.

## Features

- **Create Listings**: Users can create listings for available rooms/apartments
- **Search & Filter**: Advanced search with multiple filters (location, budget, amenities, etc.)
- **Image Management**: Upload and manage listing images (up to 10 per listing)
- **Save/Bookmark**: Users can save listings for later viewing
- **Status Management**: Listings can be ACTIVE, PAUSED, RENTED, or CLOSED

## Module Dependencies

- `common` - Shared DTOs, exceptions, and utilities
- `user` - User entity, location entities (Governorate, City), and repositories

## API Endpoints

| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| POST | `/v1/listings` | Yes | Create a new listing |
| GET | `/v1/listings/{id}` | No | Get listing by ID |
| PUT | `/v1/listings/{id}` | Yes | Update a listing (owner only) |
| DELETE | `/v1/listings/{id}` | Yes | Delete a listing (owner only) |
| GET | `/v1/listings/me` | Yes | Get current user's listings |
| GET | `/v1/listings/search` | No | Search listings with filters |
| PATCH | `/v1/listings/{id}/status` | Yes | Change listing status |
| POST | `/v1/listings/{id}/images` | Yes | Upload images to listing |
| POST | `/v1/listings/{id}/save` | Yes | Save/bookmark a listing |
| DELETE | `/v1/listings/{id}/save` | Yes | Remove listing from saved |
| GET | `/v1/listings/saved` | Yes | Get user's saved listings |
| GET | `/v1/listings/{id}/saved` | Yes | Check if listing is saved |

## Data Model

### Listing

Main entity representing a room/apartment listing with fields for:
- Basic info (title, description, rent amount)
- Property details (type, bedrooms, roommates)
- Location (governorate, city, address)
- Preferences (gender, minimum stay, availability)
- Amenities and rules (pets, smoking, bills included)

### ListingImage

Images associated with listings, supporting:
- Multiple images per listing (max 10)
- Display ordering
- Primary image designation

### SavedListing

User bookmarks for listings.

## Search Filters

The search API supports comprehensive filtering:

```json
{
  "governorateId": 1,
  "cityId": 10,
  "location": "Maadi",
  "minRent": 2000,
  "maxRent": 5000,
  "propertyType": "APARTMENT",
  "roomType": "PRIVATE",
  "preferredGender": "MALE",
  "availableFrom": "2024-03-01",
  "requiredAmenities": ["WIFI", "AIR_CONDITIONING"],
  "billsIncluded": true,
  "petsAllowed": false,
  "hasAvailableSpots": true,
  "sortBy": "price",
  "sortDirection": "asc",
  "page": 0,
  "size": 20
}
```

## Usage Examples

### Create a Listing

```bash
curl -X POST http://localhost:8081/v1/listings \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: multipart/form-data" \
  -F 'request={"title":"Cozy room in Maadi","description":"...","rentAmount":3000,...}' \
  -F "images=@room1.jpg" \
  -F "images=@room2.jpg"
```

### Search Listings

```bash
curl "http://localhost:8081/v1/listings/search?governorateId=1&minRent=2000&maxRent=5000&sortBy=price"
```

## Database Tables

- `listings` - Main listings table
- `listing_images` - Listing images
- `saved_listings` - User bookmarks

See migration files in `sakny-server/src/main/resources/db/changelog/changes/`:
- `007-create-listings-table.xml`
- `008-create-listing-images-table.xml`
- `009-create-saved-listings-table.xml`

