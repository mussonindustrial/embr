import type { ComponentEvent, Prop, PropSchema } from './periscopeApi'

export type JsonSchema = Record<string, unknown>

function withNull(schema: JsonSchema): JsonSchema {
  if (typeof schema.type === 'string') {
    const nullable: JsonSchema = { ...schema, type: [schema.type, 'null'] }

    if (Array.isArray(schema.enum)) {
      nullable.enum = [...schema.enum, null]
    }

    return nullable
  }

  const { description, default: defaultValue, ...rest } = schema
  const nullable: JsonSchema = { anyOf: [rest, { type: 'null' }] }

  if (description !== undefined) {
    nullable.description = description
  }

  if (defaultValue !== undefined) {
    nullable.default = defaultValue
  }

  return nullable
}

function toInteger(type: unknown) {
  if (Array.isArray(type)) {
    return type.map((name) => (name === 'number' ? 'integer' : name))
  }

  return 'integer'
}

export function createProp<P extends Prop<unknown>>(
  schema: JsonSchema,
  isOptional = false
): P {
  const next = (changes: JsonSchema) =>
    createProp({ ...schema, ...changes }, isOptional)

  return {
    schema,
    isOptional,
    hasDefault: schema.default !== undefined,
    describe: (description: string) => next({ description }),
    default: (value: unknown) => next({ default: value }),
    optional: () => createProp(schema, true),
    nullable: () => createProp(withNull(schema), isOptional),
    min: (value: number) => next({ minimum: value }),
    max: (value: number) => next({ maximum: value }),
    step: (value: number) => next({ multipleOf: value }),
    int: () => next({ type: toInteger(schema.type) }),
  } as unknown as P
}

export function createEvent<T>(
  schema: JsonSchema,
  description?: string
): ComponentEvent<T> {
  return {
    schema,
    description,
    describe: (text) => createEvent<T>(schema, text),
  }
}

export function defaultsOf(properties: Record<string, JsonSchema>) {
  const defaults: Record<string, unknown> = {}

  for (const [name, schema] of Object.entries(properties)) {
    if (schema.default !== undefined) {
      defaults[name] = schema.default
    }
  }

  return defaults
}

export function propsSchema(props: PropSchema) {
  return objectSchema(props, false)
}

export function objectSchema(props: PropSchema, requiresFields = true) {
  const properties: Record<string, JsonSchema> = {}
  const required: string[] = []

  for (const [name, prop] of Object.entries(props)) {
    properties[name] = prop.schema

    if (requiresFields && !prop.isOptional && !prop.hasDefault) {
      required.push(name)
    }
  }

  return {
    type: 'object',
    properties,
    ...(required.length > 0 ? { required } : {}),
  }
}
