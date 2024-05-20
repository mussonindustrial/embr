export default function isFunction(string: string): boolean {
    return /^ *<script>/.test(string)
}
