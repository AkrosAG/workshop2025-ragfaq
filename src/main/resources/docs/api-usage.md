# API Usage

AcmeCloud offers a RESTful API for interacting with your data.

## Authentication

Include your API key as a bearer token:

```http
Authorization: Bearer YOUR_API_KEY
```

## Endpoints

### Submit Data
`POST /v1/data`

Submit your data to be processed.

**Request Body:**
```json
{
  "projectId": "your_project_id",
  "data": "Your raw text or JSON"
}
```

### Retrieve Results
`GET /v1/results/{id}`

Returns the processed data result.

## Rate Limits
- Free: 100 req/day
- Pro: 10,000 req/day
- Enterprise: Unlimited

Check your usage via `GET /v1/usage`.