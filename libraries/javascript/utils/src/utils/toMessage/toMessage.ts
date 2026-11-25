export default function toMessage(error: unknown): string {
  return error instanceof Error ? error.message : String(error)
}
