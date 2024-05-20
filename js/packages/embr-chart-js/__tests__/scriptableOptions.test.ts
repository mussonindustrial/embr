import { it, expect, describe } from 'vitest'
import {
    ChartScript,
    ContextScript,
    asChartScript,
    asContextScript,
} from '../src/util'

describe('scriptableOptions', () => {
    it('run a context script', async () => {
        const script = asContextScript(
            `<script>return context + ' ' + options;`
        ) as ContextScript
        expect(script('contextValue', 'optionsValue')).toBe(
            'contextValue optionsValue'
        )
    })

    it('run a context script with extra context', async () => {
        const script = asContextScript(
            `<script>return context + ' ' + options + ' ' + extraProp;`,
            {
                extraProp: 'extraPropValue',
            }
        ) as ContextScript
        expect(script('contextValue', 'optionsValue')).toBe(
            'contextValue optionsValue extraPropValue'
        )
    })

    it('run a chart script', async () => {
        const script = asChartScript(`<script>return chart;`) as ChartScript
        expect(script('chartValue')).toBe('chartValue')
    })

    it('run a chart script with extra context', async () => {
        const script = asChartScript(
            `<script>return chart + ' ' + extraProp;`,
            {
                extraProp: 'extraPropValue',
            }
        ) as ChartScript
        expect(script('chartValue')).toBe('chartValue extraPropValue')
    })
})
