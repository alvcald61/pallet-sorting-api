# 🎨 Guía de Integración Frontend - Sistema de Gestión de Transporte

## 📋 Índice

1. [Arquitectura General](#arquitectura-general)
2. [Flujos de Usuario](#flujos-de-usuario)
3. [APIs por Módulo](#apis-por-módulo)
4. [Ejemplos de Código](#ejemplos-de-código)
5. [Estados y Transiciones](#estados-y-transiciones)
6. [Mejores Prácticas](#mejores-prácticas)

---

## 🏗️ Arquitectura General

```
┌─────────────────────────────────────────────────────────────┐
│                    FRONTEND APPLICATION                      │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   Client     │  │    Admin     │  │   Driver     │     │
│  │   Portal     │  │   Dashboard  │  │   Mobile     │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
│         │                 │                 │               │
│         └─────────────────┴─────────────────┘               │
│                          │                                   │
└──────────────────────────┼───────────────────────────────────┘
                           │
                           ▼
        ┌──────────────────────────────────────┐
        │        API GATEWAY / AXIOS           │
        │     (Authentication, Interceptors)    │
        └──────────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────────┐
│                    BACKEND REST API                          │
│                                                              │
│  /api/auth          - Autenticación                         │
│  /api/order         - Gestión de órdenes                    │
│  /api/order/{id}/transport - Tracking de transporte         │
│  /api/truck         - Gestión de camiones                   │
│  /api/client        - Gestión de clientes                   │
│  /api/driver        - Gestión de conductores                │
│  /api/warehouse     - Gestión de almacenes                  │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

## 🔄 Flujos de Usuario

### 1. Flujo de Creación de Orden (Cliente)

```
┌──────────┐
│ CLIENTE  │
└────┬─────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│ 1. LOGIN                                                     │
│    POST /api/auth/login                                     │
│    { email, password }                                       │
│    → { accessToken, refreshToken, user }                    │
└────┬────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│ 2. FORMULARIO DE ORDEN                                      │
│    - Seleccionar origen (almacén)                          │
│    - Ingresar destino (dirección completa)                 │
│    - Agregar pallets/bultos (dimensiones, peso, cantidad)  │
│    - Seleccionar fecha de recojo                           │
└────┬────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│ 3. CALCULAR DISPONIBILIDAD DE SLOTS                         │
│    GET /api/order/available-slots?date=2026-02-15          │
│    → ["08:00", "09:00", "10:00", ...]                      │
└────┬────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│ 4. CREAR Y RESOLVER PACKING                                 │
│    POST /api/order/solve/2D                                 │
│    {                                                         │
│      fromAddress: { warehouseId, address, district, ... },  │
│      toAddress: { address, district, city, state, ... },    │
│      deliveryDate: "2026-02-15T08:00:00",                  │
│      pallets: [                                             │
│        { width, length, height, weight, quantity }          │
│      ],                                                     │
│      totalVolume, totalWeight                               │
│    }                                                         │
│    → {                                                       │
│         truck: { id, licensePlate, ... },                   │
│         imageUrl: "path/to/solution.png"                    │
│       }                                                      │
└────┬────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│ 5. MOSTRAR CONFIRMACIÓN                                     │
│    - Mostrar camión asignado                               │
│    - Mostrar imagen de distribución de carga               │
│    - Mostrar precio estimado (si cliente trust)            │
│    - Botón: Confirmar Orden                                │
└────┬────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│ 6. ORDEN CREADA                                             │
│    Estado: REVIEW                                           │
│    → Redirigir a detalle de orden                          │
└─────────────────────────────────────────────────────────────┘
```

### 2. Flujo de Aprobación (Admin)

```
┌──────────┐
│  ADMIN   │
└────┬─────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│ 1. VER ÓRDENES PENDIENTES                                   │
│    GET /api/order?status=REVIEW&page=0&size=20             │
│    → { data: [...orders], pageInfo: {...} }                │
└────┬────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│ 2. VER DETALLE DE ORDEN                                     │
│    GET /api/order/{orderId}                                 │
│    → {                                                       │
│         id, client, zone, truck, status,                    │
│         packages: [...], documents: [...],                   │
│         totalVolume, totalWeight, ...                       │
│       }                                                      │
└────┬────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│ 3. CONTINUAR ORDEN (Pre-aprobar con monto)                 │
│    PUT /api/order/{orderId}/continue                        │
│    {                                                         │
│      amount: 350.00,                                        │
│      denied: false                                          │
│    }                                                         │
│    → Estado cambia a: PRE_APPROVED                         │
└────┬────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│ 4. CLIENTE ACEPTA PRECIO                                    │
│    PUT /api/order/{orderId}/continue                        │
│    { amount: null, denied: false }                          │
│    → Estado cambia a: APPROVED                             │
└────┬────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│ 5. SUBIR DOCUMENTOS (si warehouse requiere)                │
│    POST /api/order/{orderId}/documents/{docId}/upload      │
│    FormData: { file }                                       │
│    → Estado cambia a: IN_PROGRESS (cuando todos subidos)   │
└─────────────────────────────────────────────────────────────┘
```

### 3. Flujo de Transporte (Driver)

```
┌──────────┐
│ DRIVER   │
└────┬─────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│ 1. VER ÓRDENES ASIGNADAS                                   │
│    GET /api/order?page=0&size=20                           │
│    (Filtrado automático por ROLE_DRIVER)                   │
│    → { data: [...orders] }                                 │
└────┬────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│ 2. INICIAR VIAJE AL ALMACÉN                                │
│    PATCH /api/order/{orderId}/transport/status             │
│    {                                                         │
│      status: "EN_ROUTE_TO_WAREHOUSE",                      │
│      latitude: -12.0464,                                    │
│      longitude: -77.0428,                                   │
│      notes: "Saliendo hacia almacén"                       │
│    }                                                         │
└────┬────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│ 3. LLEGAR AL ALMACÉN                                        │
│    PATCH /api/order/{orderId}/transport/status             │
│    {                                                         │
│      status: "ARRIVED_AT_WAREHOUSE",                       │
│      latitude, longitude,                                   │
│      address: "Av. Argentina 123, Callao"                  │
│    }                                                         │
└────┬────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│ 4. INICIAR CARGA (con foto)                                │
│    PATCH /api/order/{orderId}/transport/status             │
│    {                                                         │
│      status: "LOADING",                                     │
│      photoUrl: "https://s3.../loading_start.jpg",          │
│      notes: "Iniciando carga de 15 pallets"                │
│    }                                                         │
└────┬────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│ 5. COMPLETAR CARGA (con foto)                              │
│    PATCH /api/order/{orderId}/transport/status             │
│    {                                                         │
│      status: "LOADING_COMPLETED",                          │
│      photoUrl: "https://s3.../loading_complete.jpg",       │
│      notes: "Carga completa: 15/15 pallets"                │
│    }                                                         │
└────┬────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│ 6. EN RUTA AL DESTINO                                       │
│    PATCH /api/order/{orderId}/transport/status             │
│    { status: "EN_ROUTE_TO_DESTINATION", ... }              │
│    (Repetir para: ARRIVED, UNLOADING, DELIVERED)           │
└─────────────────────────────────────────────────────────────┘
```

---

## 📡 APIs por Módulo

### Módulo 1: Autenticación

#### 1.1 Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "cliente@empresa.com",
  "password": "password123"
}

Response 200:
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR...",
  "tokenType": "Bearer",
  "email": "cliente@empresa.com",
  "firstName": "Juan",
  "lastName": "Pérez",
  "roles": ["CLIENT"]
}
```

#### 1.2 Refresh Token
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR..."
}

Response 200:
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR...",
  ...
}
```

### Módulo 2: Órdenes

#### 2.1 Calcular Slots Disponibles
```http
GET /api/order/available-slots?date=2026-02-15
Authorization: Bearer {accessToken}

Response 200:
["08:00", "09:00", "10:00", "11:00", "13:00", "14:00", "15:00"]
```

#### 2.2 Crear Orden (Resolver Packing)
```http
POST /api/order/solve/2D
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "fromAddress": {
    "warehouseId": 1,
    "address": "Av. Industrial 456",
    "district": "Callao",
    "city": "Lima",
    "state": "Lima"
  },
  "toAddress": {
    "address": "Av. Arequipa 1234",
    "district": "Miraflores",
    "city": "Lima",
    "state": "Lima",
    "locationLink": "https://maps.google.com/?q=-12.0464,-77.0428"
  },
  "deliveryDate": "2026-02-15T08:00:00",
  "pallets": [
    {
      "width": 1.2,
      "length": 1.0,
      "height": 1.5,
      "weight": 500.0,
      "quantity": 10,
      "volume": 1.8
    },
    {
      "width": 1.0,
      "length": 1.0,
      "height": 1.2,
      "weight": 300.0,
      "quantity": 5,
      "volume": 1.2
    }
  ],
  "totalVolume": 24.0,
  "totalWeight": 6500.0
}

Response 200:
{
  "truck": {
    "id": 5,
    "licensePlate": "ABC-123",
    "type": "TRAILER",
    "width": 2.4,
    "length": 6.0,
    "height": 2.5,
    "volume": 36.0,
    "weight": 15000.0
  },
  "imageUrl": "/uploads/bin/2026-02-15/order_123_solution.png"
}
```

#### 2.3 Listar Órdenes
```http
GET /api/order?page=0&size=20&status=IN_PROGRESS
Authorization: Bearer {accessToken}

Response 200:
{
  "success": true,
  "data": [
    {
      "id": 123,
      "orderStatus": "IN_PROGRESS",
      "transportStatus": "LOADING",
      "client": {
        "id": 10,
        "businessName": "Empresa XYZ SAC"
      },
      "truck": {
        "licensePlate": "ABC-123"
      },
      "pickupDate": "2026-02-15T08:00:00",
      "projectedDeliveryDate": "2026-02-15T10:30:00",
      "fromAddress": "Av. Industrial 456, Callao",
      "toAddress": "Av. Arequipa 1234, Miraflores",
      "totalVolume": 24.0,
      "totalWeight": 6500.0,
      "amount": 350.00
    }
  ],
  "pageInfo": {
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 45,
    "totalPages": 3
  }
}
```

#### 2.4 Ver Detalle de Orden
```http
GET /api/order/{orderId}
Authorization: Bearer {accessToken}

Response 200:
{
  "success": true,
  "data": {
    "id": 123,
    "orderStatus": "IN_PROGRESS",
    "transportStatus": "LOADING",
    "client": { ... },
    "zone": { ... },
    "truck": { ... },
    "driver": {
      "driverId": 5,
      "firstName": "Carlos",
      "lastName": "Rodríguez",
      "phone": "+51999888777"
    },
    "warehouse": { ... },
    "pickupDate": "2026-02-15T08:00:00",
    "projectedDeliveryDate": "2026-02-15T10:30:00",
    "fromAddress": "...",
    "toAddress": "...",
    "addressLink": "https://maps.google.com/...",
    "gpsLink": null,
    "packages": [
      {
        "width": 1.2,
        "length": 1.0,
        "height": 1.5,
        "weight": 500.0,
        "quantity": 10
      }
    ],
    "documents": [
      {
        "documentId": 1,
        "documentName": "Guía de Remisión",
        "link": "https://s3.../guia_123.pdf",
        "required": true
      }
    ],
    "totalVolume": 24.0,
    "totalWeight": 6500.0,
    "amount": 350.00,
    "solutionImageUrl": "/uploads/bin/.../solution.png"
  }
}
```

#### 2.5 Continuar Orden (Cambiar Estado)
```http
PUT /api/order/{orderId}/continue
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "amount": 350.00,
  "gpsLink": null,
  "denied": false
}

Response 200:
{
  "success": true,
  "message": "Order status updated successfully"
}
```

#### 2.6 Subir Documento
```http
POST /api/order/{orderId}/documents/{documentId}/upload
Authorization: Bearer {accessToken}
Content-Type: multipart/form-data

FormData: { file: <archivo> }

Response 200:
{
  "success": true,
  "data": "https://s3.../123-1-guia_remision.pdf"
}
```

#### 2.7 Ver Historial de Estados
```http
GET /api/order/{orderId}/status
Authorization: Bearer {accessToken}

Response 200:
{
  "success": true,
  "data": [
    {
      "status": "IN_PROGRESS",
      "createdAt": "2026-02-15T07:45:00"
    },
    {
      "status": "DOCUMENT_PENDING",
      "createdAt": "2026-02-15T07:30:00"
    },
    {
      "status": "APPROVED",
      "createdAt": "2026-02-15T07:15:00"
    }
  ]
}
```

### Módulo 3: Tracking de Transporte

#### 3.1 Actualizar Estado de Transporte
```http
PATCH /api/order/{orderId}/transport/status
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "status": "LOADING",
  "latitude": -12.0464,
  "longitude": -77.0428,
  "address": "Av. Argentina 123, Callao",
  "notes": "Iniciando carga de 15 pallets",
  "photoUrl": "https://s3.amazonaws.com/.../photo123.jpg"
}

Response 200:
{
  "success": true,
  "message": "Transport status updated successfully to: Cargando"
}
```

#### 3.2 Actualización Rápida
```http
PATCH /api/order/{orderId}/transport/status/quick?status=TRUCK_ASSIGNED
Authorization: Bearer {accessToken}

Response 200:
{
  "success": true,
  "message": "Transport status updated successfully to: Camión Asignado"
}
```

#### 3.3 Ver Historial de Transporte
```http
GET /api/order/{orderId}/transport/history
Authorization: Bearer {accessToken}

Response 200:
{
  "success": true,
  "data": [
    {
      "id": 15,
      "status": "LOADING",
      "statusDisplayName": "Cargando",
      "timestamp": "2026-02-15T08:15:00",
      "locationLatitude": -12.0464,
      "locationLongitude": -77.0428,
      "locationAddress": "Av. Argentina 123, Callao",
      "notes": "Iniciando carga de 15 pallets",
      "updatedBy": "driver@tupack.com",
      "photoUrl": "https://s3.../photo123.jpg",
      "signatureUrl": null
    },
    {
      "id": 14,
      "status": "ARRIVED_AT_WAREHOUSE",
      "statusDisplayName": "Llegó al Almacén",
      "timestamp": "2026-02-15T08:00:00",
      ...
    }
  ]
}
```

#### 3.4 Ver Timeline con GPS
```http
GET /api/order/{orderId}/transport/timeline
Authorization: Bearer {accessToken}

Response 200:
{
  "success": true,
  "data": [
    {
      "status": "LOADING",
      "timestamp": "2026-02-15T08:15:00",
      "locationLatitude": -12.0464,
      "locationLongitude": -77.0428,
      "locationAddress": "Av. Argentina 123, Callao"
    },
    {
      "status": "ARRIVED_AT_WAREHOUSE",
      "timestamp": "2026-02-15T08:00:00",
      "locationLatitude": -12.0464,
      "locationLongitude": -77.0428,
      "locationAddress": "Av. Argentina 123, Callao"
    }
  ]
}
```

#### 3.5 Ver Estado Actual de Transporte
```http
GET /api/order/{orderId}/transport/status
Authorization: Bearer {accessToken}

Response 200:
{
  "success": true,
  "data": "LOADING"
}
```

---

## 💻 Ejemplos de Código Frontend

### React/TypeScript - Servicio de API

```typescript
// services/api.ts
import axios, { AxiosInstance } from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

class ApiService {
  private client: AxiosInstance;

  constructor() {
    this.client = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Request interceptor - añadir token
    this.client.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('accessToken');
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );

    // Response interceptor - refresh token
    this.client.interceptors.response.use(
      (response) => response,
      async (error) => {
        const originalRequest = error.config;

        if (error.response?.status === 401 && !originalRequest._retry) {
          originalRequest._retry = true;

          try {
            const refreshToken = localStorage.getItem('refreshToken');
            const response = await axios.post(`${API_BASE_URL}/auth/refresh`, {
              refreshToken,
            });

            const { accessToken } = response.data;
            localStorage.setItem('accessToken', accessToken);

            originalRequest.headers.Authorization = `Bearer ${accessToken}`;
            return this.client(originalRequest);
          } catch (refreshError) {
            // Logout user
            localStorage.clear();
            window.location.href = '/login';
            return Promise.reject(refreshError);
          }
        }

        return Promise.reject(error);
      }
    );
  }

  // Auth
  async login(email: string, password: string) {
    const response = await this.client.post('/auth/login', { email, password });
    const { accessToken, refreshToken, ...user } = response.data;

    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
    localStorage.setItem('user', JSON.stringify(user));

    return response.data;
  }

  async logout() {
    localStorage.clear();
  }

  // Orders
  async getAvailableSlots(date: string) {
    const response = await this.client.get('/order/available-slots', {
      params: { date },
    });
    return response.data;
  }

  async createOrder(packingType: string, request: CreateOrderRequest) {
    const response = await this.client.post(`/order/solve/${packingType}`, request);
    return response.data;
  }

  async getOrders(params?: {
    page?: number;
    size?: number;
    status?: string;
  }) {
    const response = await this.client.get('/order', { params });
    return response.data;
  }

  async getOrderById(orderId: number) {
    const response = await this.client.get(`/order/${orderId}`);
    return response.data;
  }

  async continueOrder(orderId: number, data: ContinueOrderRequest) {
    const response = await this.client.put(`/order/${orderId}/continue`, data);
    return response.data;
  }

  async uploadDocument(orderId: number, documentId: number, file: File) {
    const formData = new FormData();
    formData.append('file', file);

    const response = await this.client.post(
      `/order/${orderId}/documents/${documentId}/upload`,
      formData,
      {
        headers: { 'Content-Type': 'multipart/form-data' },
      }
    );
    return response.data;
  }

  // Transport
  async updateTransportStatus(
    orderId: number,
    request: TransportStatusUpdateRequest
  ) {
    const response = await this.client.patch(
      `/order/${orderId}/transport/status`,
      request
    );
    return response.data;
  }

  async getTransportHistory(orderId: number) {
    const response = await this.client.get(`/order/${orderId}/transport/history`);
    return response.data;
  }

  async getTransportTimeline(orderId: number) {
    const response = await this.client.get(`/order/${orderId}/transport/timeline`);
    return response.data;
  }
}

export const apiService = new ApiService();
```

### React - Componente de Creación de Orden

```typescript
// components/CreateOrderForm.tsx
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiService } from '../services/api';

interface Pallet {
  width: number;
  length: number;
  height: number;
  weight: number;
  quantity: number;
  volume: number;
}

export function CreateOrderForm() {
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [loading, setLoading] = useState(false);

  // Form state
  const [warehouses, setWarehouses] = useState([]);
  const [selectedWarehouse, setSelectedWarehouse] = useState(null);
  const [destination, setDestination] = useState({
    address: '',
    district: '',
    city: 'Lima',
    state: 'Lima',
    locationLink: '',
  });
  const [deliveryDate, setDeliveryDate] = useState('');
  const [deliveryTime, setDeliveryTime] = useState('');
  const [availableSlots, setAvailableSlots] = useState<string[]>([]);
  const [pallets, setPallets] = useState<Pallet[]>([
    { width: 1.2, length: 1.0, height: 1.5, weight: 500, quantity: 1, volume: 1.8 },
  ]);

  // Solution state
  const [solution, setSolution] = useState(null);

  // Load warehouses on mount
  useEffect(() => {
    loadWarehouses();
  }, []);

  // Load available slots when date changes
  useEffect(() => {
    if (deliveryDate) {
      loadAvailableSlots(deliveryDate);
    }
  }, [deliveryDate]);

  const loadWarehouses = async () => {
    try {
      const response = await apiService.getWarehouses();
      setWarehouses(response.data);
    } catch (error) {
      console.error('Error loading warehouses:', error);
    }
  };

  const loadAvailableSlots = async (date: string) => {
    try {
      const slots = await apiService.getAvailableSlots(date);
      setAvailableSlots(slots);
    } catch (error) {
      console.error('Error loading slots:', error);
    }
  };

  const addPallet = () => {
    setPallets([
      ...pallets,
      { width: 1.2, length: 1.0, height: 1.5, weight: 500, quantity: 1, volume: 1.8 },
    ]);
  };

  const removePallet = (index: number) => {
    setPallets(pallets.filter((_, i) => i !== index));
  };

  const updatePallet = (index: number, field: keyof Pallet, value: number) => {
    const updated = [...pallets];
    updated[index] = { ...updated[index], [field]: value };

    // Recalcular volumen
    if (['width', 'length', 'height'].includes(field)) {
      updated[index].volume =
        updated[index].width * updated[index].length * updated[index].height;
    }

    setPallets(updated);
  };

  const calculateTotals = () => {
    const totalVolume = pallets.reduce(
      (sum, p) => sum + p.volume * p.quantity,
      0
    );
    const totalWeight = pallets.reduce(
      (sum, p) => sum + p.weight * p.quantity,
      0
    );
    return { totalVolume, totalWeight };
  };

  const handleSubmit = async () => {
    setLoading(true);

    try {
      const { totalVolume, totalWeight } = calculateTotals();
      const deliveryDateTime = `${deliveryDate}T${deliveryTime}:00`;

      const request = {
        fromAddress: {
          warehouseId: selectedWarehouse.warehouseId,
          address: selectedWarehouse.address,
          district: 'Callao',
          city: 'Lima',
          state: 'Lima',
        },
        toAddress: destination,
        deliveryDate: deliveryDateTime,
        pallets,
        totalVolume,
        totalWeight,
      };

      const result = await apiService.createOrder('2D', request);
      setSolution(result);
      setStep(2); // Ir a confirmación
    } catch (error) {
      console.error('Error creating order:', error);
      alert('Error al crear la orden. Por favor intente nuevamente.');
    } finally {
      setLoading(false);
    }
  };

  const handleConfirm = () => {
    navigate('/orders');
  };

  if (step === 2 && solution) {
    return (
      <div className="confirmation-screen">
        <h2>¡Orden Creada Exitosamente!</h2>

        <div className="solution-details">
          <h3>Camión Asignado</h3>
          <p>
            <strong>Placa:</strong> {solution.truck.licensePlate}
          </p>
          <p>
            <strong>Tipo:</strong> {solution.truck.type}
          </p>
          <p>
            <strong>Dimensiones:</strong> {solution.truck.width} x{' '}
            {solution.truck.length} x {solution.truck.height} m
          </p>
        </div>

        <div className="solution-image">
          <h3>Distribución de Carga</h3>
          <img src={solution.imageUrl} alt="Distribución" />
        </div>

        <button onClick={handleConfirm}>Ver Mis Órdenes</button>
      </div>
    );
  }

  return (
    <div className="create-order-form">
      <h2>Nueva Orden de Transporte</h2>

      {/* Step 1: Información básica */}
      <div className="form-section">
        <h3>1. Origen</h3>
        <select
          value={selectedWarehouse?.warehouseId || ''}
          onChange={(e) => {
            const wh = warehouses.find(
              (w) => w.warehouseId === parseInt(e.target.value)
            );
            setSelectedWarehouse(wh);
          }}
        >
          <option value="">Seleccionar almacén</option>
          {warehouses.map((wh) => (
            <option key={wh.warehouseId} value={wh.warehouseId}>
              {wh.name} - {wh.address}
            </option>
          ))}
        </select>
      </div>

      <div className="form-section">
        <h3>2. Destino</h3>
        <input
          type="text"
          placeholder="Dirección completa"
          value={destination.address}
          onChange={(e) =>
            setDestination({ ...destination, address: e.target.value })
          }
        />
        <input
          type="text"
          placeholder="Distrito"
          value={destination.district}
          onChange={(e) =>
            setDestination({ ...destination, district: e.target.value })
          }
        />
        <input
          type="text"
          placeholder="Link de Google Maps (opcional)"
          value={destination.locationLink}
          onChange={(e) =>
            setDestination({ ...destination, locationLink: e.target.value })
          }
        />
      </div>

      <div className="form-section">
        <h3>3. Fecha y Hora de Recojo</h3>
        <input
          type="date"
          value={deliveryDate}
          onChange={(e) => setDeliveryDate(e.target.value)}
          min={new Date().toISOString().split('T')[0]}
        />
        <select
          value={deliveryTime}
          onChange={(e) => setDeliveryTime(e.target.value)}
          disabled={!availableSlots.length}
        >
          <option value="">Seleccionar hora</option>
          {availableSlots.map((slot) => (
            <option key={slot} value={slot}>
              {slot}
            </option>
          ))}
        </select>
      </div>

      <div className="form-section">
        <h3>4. Pallets/Carga</h3>
        {pallets.map((pallet, index) => (
          <div key={index} className="pallet-row">
            <input
              type="number"
              placeholder="Ancho (m)"
              value={pallet.width}
              onChange={(e) =>
                updatePallet(index, 'width', parseFloat(e.target.value))
              }
            />
            <input
              type="number"
              placeholder="Largo (m)"
              value={pallet.length}
              onChange={(e) =>
                updatePallet(index, 'length', parseFloat(e.target.value))
              }
            />
            <input
              type="number"
              placeholder="Alto (m)"
              value={pallet.height}
              onChange={(e) =>
                updatePallet(index, 'height', parseFloat(e.target.value))
              }
            />
            <input
              type="number"
              placeholder="Peso (kg)"
              value={pallet.weight}
              onChange={(e) =>
                updatePallet(index, 'weight', parseFloat(e.target.value))
              }
            />
            <input
              type="number"
              placeholder="Cantidad"
              value={pallet.quantity}
              onChange={(e) =>
                updatePallet(index, 'quantity', parseInt(e.target.value))
              }
            />
            <span>Vol: {pallet.volume.toFixed(2)} m³</span>
            <button onClick={() => removePallet(index)}>Eliminar</button>
          </div>
        ))}
        <button onClick={addPallet}>+ Agregar Pallet</button>
      </div>

      <div className="totals">
        <p>
          <strong>Volumen Total:</strong> {calculateTotals().totalVolume.toFixed(2)} m³
        </p>
        <p>
          <strong>Peso Total:</strong> {calculateTotals().totalWeight.toFixed(2)} kg
        </p>
      </div>

      <button
        onClick={handleSubmit}
        disabled={
          loading ||
          !selectedWarehouse ||
          !destination.address ||
          !deliveryDate ||
          !deliveryTime ||
          pallets.length === 0
        }
      >
        {loading ? 'Procesando...' : 'Crear Orden'}
      </button>
    </div>
  );
}
```

### React - Componente de Tracking

```typescript
// components/OrderTracking.tsx
import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { GoogleMap, Marker, Polyline } from '@react-google-maps/api';
import { apiService } from '../services/api';

interface TransportUpdate {
  id: number;
  status: string;
  statusDisplayName: string;
  timestamp: string;
  locationLatitude?: number;
  locationLongitude?: number;
  locationAddress?: string;
  notes?: string;
  updatedBy: string;
  photoUrl?: string;
}

export function OrderTracking() {
  const { orderId } = useParams<{ orderId: string }>();
  const [order, setOrder] = useState(null);
  const [timeline, setTimeline] = useState<TransportUpdate[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();

    // Poll for updates every 30 seconds
    const interval = setInterval(loadData, 30000);

    return () => clearInterval(interval);
  }, [orderId]);

  const loadData = async () => {
    try {
      const [orderResponse, timelineResponse] = await Promise.all([
        apiService.getOrderById(parseInt(orderId!)),
        apiService.getTransportTimeline(parseInt(orderId!)),
      ]);

      setOrder(orderResponse.data);
      setTimeline(timelineResponse.data);
    } catch (error) {
      console.error('Error loading data:', error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusIcon = (status: string) => {
    const icons = {
      TRUCK_ASSIGNED: '🚚',
      EN_ROUTE_TO_WAREHOUSE: '🛣️',
      ARRIVED_AT_WAREHOUSE: '🏭',
      LOADING: '📦',
      LOADING_COMPLETED: '✅',
      EN_ROUTE_TO_DESTINATION: '🚛',
      ARRIVED_AT_DESTINATION: '📍',
      UNLOADING: '📤',
      DELIVERED: '🎉',
    };
    return icons[status] || '⏳';
  };

  if (loading) {
    return <div>Cargando...</div>;
  }

  const center = timeline[0]?.locationLatitude
    ? {
        lat: timeline[0].locationLatitude,
        lng: timeline[0].locationLongitude,
      }
    : { lat: -12.0464, lng: -77.0428 };

  return (
    <div className="order-tracking">
      <div className="header">
        <h2>Tracking de Orden #{orderId}</h2>
        <div className="current-status">
          <span className="icon">
            {getStatusIcon(order.transportStatus)}
          </span>
          <span className="status-text">
            {timeline[0]?.statusDisplayName || 'Pendiente'}
          </span>
        </div>
      </div>

      <div className="content">
        {/* Map */}
        <div className="map-container">
          <GoogleMap mapContainerStyle={{ width: '100%', height: '400px' }} center={center} zoom={12}>
            {/* Route polyline */}
            <Polyline
              path={timeline
                .filter((t) => t.locationLatitude && t.locationLongitude)
                .map((t) => ({
                  lat: t.locationLatitude!,
                  lng: t.locationLongitude!,
                }))}
              options={{
                strokeColor: '#2196F3',
                strokeWeight: 3,
              }}
            />

            {/* Markers */}
            {timeline.map((update) =>
              update.locationLatitude && update.locationLongitude ? (
                <Marker
                  key={update.id}
                  position={{
                    lat: update.locationLatitude,
                    lng: update.locationLongitude,
                  }}
                  label={{
                    text: getStatusIcon(update.status),
                    fontSize: '24px',
                  }}
                  title={update.statusDisplayName}
                />
              ) : null
            )}
          </GoogleMap>
        </div>

        {/* Timeline */}
        <div className="timeline">
          <h3>Historial</h3>
          {timeline.map((update) => (
            <div key={update.id} className="timeline-item">
              <div className="timeline-marker">
                {getStatusIcon(update.status)}
              </div>
              <div className="timeline-content">
                <h4>{update.statusDisplayName}</h4>
                <p className="timestamp">
                  {new Date(update.timestamp).toLocaleString('es-PE')}
                </p>
                {update.locationAddress && (
                  <p className="address">📍 {update.locationAddress}</p>
                )}
                {update.notes && <p className="notes">{update.notes}</p>}
                {update.updatedBy && (
                  <p className="updated-by">Por: {update.updatedBy}</p>
                )}
                {update.photoUrl && (
                  <a
                    href={update.photoUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    Ver foto 📷
                  </a>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
```

---

## 🔄 Estados y Transiciones

### Diagrama de Estados de Orden

```
┌────────────────────────────────────────────────────────────┐
│                    OrderStatus Flow                         │
└────────────────────────────────────────────────────────────┘

    ┌──────────┐
    │  REVIEW  │  ← Estado inicial
    └────┬─────┘
         │ Admin: continueOrder(amount)
         ▼
┌────────────────┐
│  PRE_APPROVED  │  Con precio propuesto
└────┬───────────┘
     │ Client: continueOrder() - acepta precio
     ▼
┌──────────┐
│ APPROVED │  Orden aprobada
└────┬─────┘
     │
     ├─► (Si warehouse.hasDocuments)
     │   ┌──────────────────┐
     │   │ DOCUMENT_PENDING │
     │   └────┬─────────────┘
     │        │ Client: uploadDocuments()
     │        └────────┐
     │                 │
     └─────────────────┤
                       ▼
                ┌──────────────┐
                │ IN_PROGRESS  │  ← Orden activa
                └──────┬───────┘
                       │ Transport completo
                       ▼
                ┌──────────┐
                │ DELIVERED│  ← Estado final exitoso
                └──────────┘

    (Desde cualquier estado antes de IN_PROGRESS)
    ┌─────────┐
    │ DENIED  │  ← Estado final - orden rechazada
    └─────────┘
```

### Diagrama de Estados de Transporte

```
┌────────────────────────────────────────────────────────────┐
│              TransportStatus Flow (Granular)                │
└────────────────────────────────────────────────────────────┘

    ┌──────────┐
    │ PENDING  │  ← Camión asignado pero sin iniciar
    └────┬─────┘
         │ Driver: updateStatus("TRUCK_ASSIGNED")
         ▼
┌─────────────────┐
│ TRUCK_ASSIGNED  │
└────┬────────────┘
     │ Driver: updateStatus("EN_ROUTE_TO_WAREHOUSE")
     ▼
┌─────────────────────────┐
│ EN_ROUTE_TO_WAREHOUSE  │  🚛 En tránsito
└────┬────────────────────┘
     │ Driver: updateStatus("ARRIVED_AT_WAREHOUSE")
     ▼
┌───────────────────────┐
│ ARRIVED_AT_WAREHOUSE  │  📍 En ubicación
└────┬──────────────────┘
     │ Driver: updateStatus("LOADING")
     ▼
┌───────────┐
│  LOADING  │  📦 Operación activa
└────┬──────┘
     │ Driver: updateStatus("LOADING_COMPLETED")
     ▼
┌────────────────────┐
│ LOADING_COMPLETED  │  ✅ Operación completa
└────┬───────────────┘
     │ Driver: updateStatus("EN_ROUTE_TO_DESTINATION")
     ▼
┌────────────────────────────┐
│ EN_ROUTE_TO_DESTINATION   │  🚛 En tránsito
└────┬───────────────────────┘
     │ Driver: updateStatus("ARRIVED_AT_DESTINATION")
     ▼
┌──────────────────────────┐
│ ARRIVED_AT_DESTINATION  │  📍 En ubicación
└────┬─────────────────────┘
     │ Driver: updateStatus("UNLOADING")
     ▼
┌─────────────┐
│  UNLOADING  │  📤 Operación activa
└────┬────────┘
     │ Driver: updateStatus("UNLOADING_COMPLETED")
     ▼
┌──────────────────────┐
│ UNLOADING_COMPLETED  │  ✅ Operación completa
└────┬─────────────────┘
     │ Driver: updateStatus("DELIVERED")
     ▼
┌────────────┐
│ DELIVERED  │  🎉 Estado final
└────────────┘
│
└──► OrderStatus también cambia a DELIVERED
```

---

## 🎯 Mejores Prácticas

### 1. Manejo de Tokens

```typescript
// Siempre verificar expiración antes de hacer requests
function isTokenExpired(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.exp * 1000 < Date.now();
  } catch {
    return true;
  }
}

// Auto-refresh si está por expirar
async function ensureValidToken() {
  const token = localStorage.getItem('accessToken');
  if (!token || isTokenExpired(token)) {
    await apiService.refreshToken();
  }
}
```

### 2. Polling Inteligente

```typescript
// Usar polling solo cuando sea necesario
function useOrderPolling(orderId: number, enabled: boolean) {
  useEffect(() => {
    if (!enabled) return;

    const interval = setInterval(async () => {
      await loadOrderData(orderId);
    }, 30000); // 30 segundos

    return () => clearInterval(interval);
  }, [orderId, enabled]);
}

// Habilitar solo para órdenes IN_PROGRESS
const isActive = order.orderStatus === 'IN_PROGRESS';
useOrderPolling(orderId, isActive);
```

### 3. Optimistic Updates

```typescript
// Actualizar UI inmediatamente, revertir si falla
async function updateTransportStatus(orderId: number, status: string) {
  // Guardar estado actual
  const previousStatus = currentStatus;

  // Actualizar UI optimisticamente
  setCurrentStatus(status);

  try {
    // Hacer request
    await apiService.updateTransportStatus(orderId, { status });
  } catch (error) {
    // Revertir si falla
    setCurrentStatus(previousStatus);
    showError('Error al actualizar estado');
  }
}
```

### 4. Caché de Datos

```typescript
// Cachear datos que no cambian frecuentemente
const warehousesCache = {
  data: null,
  timestamp: null,
  TTL: 5 * 60 * 1000, // 5 minutos

  async get() {
    if (
      this.data &&
      this.timestamp &&
      Date.now() - this.timestamp < this.TTL
    ) {
      return this.data;
    }

    this.data = await apiService.getWarehouses();
    this.timestamp = Date.now();
    return this.data;
  },

  invalidate() {
    this.data = null;
    this.timestamp = null;
  },
};
```

### 5. Manejo de Errores

```typescript
// Centralizar manejo de errores
function handleApiError(error: any) {
  if (error.response) {
    // Error con respuesta del servidor
    const { status, data } = error.response;

    switch (status) {
      case 400:
        showError(data.message || 'Solicitud inválida');
        break;
      case 401:
        showError('Sesión expirada');
        redirectToLogin();
        break;
      case 403:
        showError('No tienes permisos para esta acción');
        break;
      case 404:
        showError(data.message || 'Recurso no encontrado');
        break;
      case 500:
        showError('Error del servidor. Por favor intente más tarde.');
        break;
      default:
        showError('Error inesperado');
    }
  } else if (error.request) {
    // Request hecho pero sin respuesta
    showError('No se pudo conectar con el servidor');
  } else {
    // Error al configurar request
    showError('Error al procesar la solicitud');
  }
}
```

---

## 📱 Responsive Design

### Breakpoints Recomendados

```css
/* Mobile */
@media (max-width: 768px) {
  .create-order-form {
    padding: 1rem;
  }

  .pallet-row {
    flex-direction: column;
  }

  .map-container {
    height: 300px;
  }
}

/* Tablet */
@media (min-width: 769px) and (max-width: 1024px) {
  .timeline {
    max-width: 600px;
  }
}

/* Desktop */
@media (min-width: 1025px) {
  .order-tracking {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: 2rem;
  }
}
```

---

## 🚀 Performance Tips

1. **Lazy Loading**: Cargar componentes pesados solo cuando se necesiten
2. **Code Splitting**: Dividir bundles por rutas
3. **Image Optimization**: Comprimir imágenes de soluciones de packing
4. **Debouncing**: En búsquedas y auto-complete
5. **Memoization**: Usar `useMemo` y `useCallback` para evitar re-renders

---

## ✅ Resumen

Este documento proporciona:

- ✅ Arquitectura completa de integración frontend-backend
- ✅ Flujos de usuario paso a paso con diagramas
- ✅ APIs documentadas con ejemplos de request/response
- ✅ Código completo de ejemplo en React/TypeScript
- ✅ Diagramas de estados y transiciones
- ✅ Mejores prácticas de implementación

**Para implementar el frontend**, sigue los flujos documentados y utiliza los servicios de API proporcionados. Cada endpoint está diseñado para un propósito específico y retorna datos estructurados listos para usar.
