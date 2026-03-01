# Order Domain Services

Este directorio está reservado para servicios del dominio (Domain Services).

## Propósito

Los Domain Services encapsulan lógica de negocio que:
- No pertenece naturalmente a una entidad específica
- Opera sobre múltiples entidades
- Implementa reglas de negocio del dominio

## Ejemplos de Uso Futuro

- PricingDomainService: Cálculos de precios basados en reglas de negocio
- OrderValidationService: Validaciones complejas de órdenes
- PackingStrategyService: Estrategias de empaquetado de pallets

## Diferencia con Application Services

- **Domain Services**: Lógica de negocio pura, sin dependencias de infraestructura
- **Application Services**: Orquestación, transacciones, llamadas a infraestructura
