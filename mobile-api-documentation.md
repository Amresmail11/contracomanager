# Mobile API Documentation

## Authentication Endpoints

### Register
```http
POST /api/auth/register
```

**Request Body:**
```json
{
  "email": "user@example.com",
  "username": "username",
  "password": "password",
  "job": "Engineer"
}
```

**Success Response:**
```json
{
  "status": "success",
  "message": "User registered successfully",
  "user": {
    "id": "uuid",
    "username": "username",
    "email": "user@example.com",
    "job": "Engineer"
  }
}
```

### Login
```http
POST /api/auth/login
```

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password"
}
```

**Success Response:**
```json
{
  "status": "success",
  "token": "JWT_TOKEN",
  "user": {
    "id": "uuid",
    "username": "username",
    "email": "user@example.com",
    "job": "Engineer"
  }
}
```

## Project Endpoints

### Get My Projects
```http
GET /api/projects/my-projects
```

**Success Response:**
```json
{
  "status": "success",
  "projects": [
    {
      "name": "Project Name",
      "code": "PROJ-123",
      "dueDate": "2024-02-01T00:00:00Z",
      "projectOwner": "Owner Name",
      "address": "Project Address"
    }
  ]
}
```

### Join Project
```http
POST /api/projects/join/{projectCode}
```

**Path Parameters:**
- `projectCode`: Project code in format PROJ-XXX (e.g., PROJ-123)

**Success Response:**
```json
{
  "status": "success",
  "message": "Successfully joined the project",
  "project": {
    "name": "Project Name",
    "code": "PROJ-123",
    "owner": "Project Owner"
  }
}
```

**Error Responses:**
```json
{
  "status": "error",
  "message": "Invalid project code format. Must be in format PROJ-XXX where X is a digit"
}
```
```json
{
  "status": "error",
  "message": "You are already a member of this project"
}
```

## Group Endpoints

### Get User Groups
```http
GET /api/groups/user-groups
```

**Success Response:**
```json
{
  "status": "success",
  "totalGroups": 2,
  "totalMembers": 10,
  "groups": [
    {
      "id": "uuid",
      "name": "Group Name",
      "projectCode": "PROJ-123",
      "projectName": "Project Name",
      "createdById": "uuid",
      "createdByUsername": "username",
      "createdAt": "2024-01-26T12:00:00Z",
      "totalMembers": 5,
      "members": [
        {
          "id": "uuid",
          "username": "username",
          "email": "user@example.com",
          "job": "Engineer"
        }
      ]
    }
  ]
}
```

### Get Project Groups
```http
GET /api/groups/project/{projectCode}
```

**Path Parameters:**
- `projectCode`: Project code (e.g., PROJ-123)

**Success Response:**
```json
{
  "status": "success",
  "groups": [
    {
      "id": "uuid",
      "name": "Group Name",
      "totalMembers": 5,
      "members": [
        {
          "id": "uuid",
          "username": "username",
          "email": "user@example.com",
          "job": "Engineer"
        }
      ]
    }
  ]
}
```

## RFI (Request for Information) Endpoints

### Get User's RFIs
```http
GET /api/rfis/user
```

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Items per page (default: 20)

**Success Response:**
```json
{
  "status": "success",
  "content": [
    {
      "id": "uuid",
      "title": "RFI Title",
      "description": "RFI Description",
      "status": "OPEN",
      "priority": "HIGH",
      "category": "TECHNICAL",
      "dueDate": "2024-02-01T00:00:00Z",
      "createdAt": "2024-01-26T12:00:00Z",
      "createdBy": {
        "id": "uuid",
        "username": "username"
      },
      "assignedTo": {
        "id": "uuid",
        "username": "username"
      },
      "project": {
        "id": "uuid",
        "name": "Project Name",
        "code": "PROJ-123"
      }
    }
  ],
  "totalElements": 50,
  "totalPages": 3,
  "currentPage": 0,
  "size": 20
}
```

### Get Project RFIs
```http
GET /api/rfis/project/{projectCode}
```

**Path Parameters:**
- `projectCode`: Project code (e.g., PROJ-123)

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Items per page (default: 20)
- `status`: Filter by status (optional: OPEN, IN_PROGRESS, CLOSED)

**Success Response:**
```json
{
  "status": "success",
  "content": [
    {
      "id": "uuid",
      "title": "RFI Title",
      "description": "RFI Description",
      "status": "OPEN",
      "priority": "HIGH",
      "category": "TECHNICAL",
      "dueDate": "2024-02-01T00:00:00Z",
      "createdAt": "2024-01-26T12:00:00Z",
      "createdBy": {
        "id": "uuid",
        "username": "username"
      },
      "assignedTo": {
        "id": "uuid",
        "username": "username"
      }
    }
  ],
  "totalElements": 50,
  "totalPages": 3,
  "currentPage": 0,
  "size": 20
}
```

### Get RFI Details
```http
GET /api/rfis/{rfiId}
```

**Path Parameters:**
- `rfiId`: RFI UUID

**Success Response:**
```json
{
  "status": "success",
  "rfi": {
    "id": "uuid",
    "title": "RFI Title",
    "description": "RFI Description",
    "status": "OPEN",
    "priority": "HIGH",
    "category": "TECHNICAL",
    "dueDate": "2024-02-01T00:00:00Z",
    "createdAt": "2024-01-26T12:00:00Z",
    "createdBy": {
      "id": "uuid",
      "username": "username"
    },
    "assignedTo": {
      "id": "uuid",
      "username": "username"
    },
    "project": {
      "id": "uuid",
      "name": "Project Name",
      "code": "PROJ-123"
    },
    "replies": [
      {
        "id": "uuid",
        "content": "Reply content",
        "createdAt": "2024-01-26T12:00:00Z",
        "createdBy": {
          "id": "uuid",
          "username": "username"
        }
      }
    ]
  }
}
```

### Get Overdue RFIs
```http
GET /api/rfis/overdue
```

**Success Response:**
```json
{
  "status": "success",
  "rfis": [
    {
      "id": "uuid",
      "title": "RFI Title",
      "description": "RFI Description",
      "status": "OPEN",
      "priority": "HIGH",
      "dueDate": "2024-01-25T00:00:00Z",
      "project": {
        "id": "uuid",
        "name": "Project Name",
        "code": "PROJ-123"
      }
    }
  ]
}
```

## Common Error Responses

### Unauthorized
```json
{
  "status": "error",
  "message": "Unauthorized"
}
```

### Not Found
```json
{
  "status": "error",
  "message": "Resource not found"
}
```

### Server Error
```json
{
  "status": "error",
  "message": "Internal server error"
}
```

## Authentication

All endpoints except `/api/auth/login` and `/api/auth/register` require authentication.
Include the JWT token in the Authorization header:

```http
Authorization: Bearer <JWT_TOKEN>
``` 