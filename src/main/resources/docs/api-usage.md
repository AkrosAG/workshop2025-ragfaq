# API Nutzung

AcmeCloud bietet eine RESTful API zur Interaktion mit Ihren Daten.

## Authentifizierung

Fügen Sie Ihren API-Schlüssel als Bearer-Token hinzu:

```http
Authorization: Bearer YOUR_API_KEY
```

## Endpunkte

### Daten übermitteln
`POST /v1/data`

Übermitteln Sie Ihre Daten zur Verarbeitung.

**Request Body:**
```json
{
  "projectId": "your_project_id",
  "data": "Your raw text or JSON"
}
```

### Ergebnisse abrufen
`GET /v1/results/{id}`

Gibt das verarbeitete Daten-Ergebnis zurück.

## Anfrage-Limits
- Free: 100 Anfragen/Tag
- Pro: 10.000 Anfragen/Tag
- Enterprise: Unbegrenzt

Überprüfen Sie Ihre Nutzung mit `GET /v1/usage`.
