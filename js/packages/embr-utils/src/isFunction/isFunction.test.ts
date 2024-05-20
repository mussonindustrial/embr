import { it, expect, describe } from 'vitest'

import isFunction from './isFunction'

describe('isFunction', () => {
    it('false if no script tag', async () => {
        expect(isFunction('no script tag here')).toBe(false)
    })

    it('true if script tag', async () => {
        expect(isFunction('<script> script tag here')).toBe(true)
    })

    it('true if script tag (with extra spaces)', async () => {
        expect(isFunction('    <script> script tag here')).toBe(true)
    })
})
