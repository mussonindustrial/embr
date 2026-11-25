package com.mussonindustrial.ignition.embr.periscope.js

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class JavaScriptImportScannerTest :
    DescribeSpec(
        {
            fun specifiers(source: String): List<String> {
                return JavaScriptImportScanner(source).scan().map { it.specifier }
            }

            describe("JavaScriptImportScanner") {
                it("finds static imports") {
                    val source =
                        """
                        import * as React from 'react';
                        import defaultExport, { a, b as c } from "@/lib/util";
                        import './side-effect';
                        import{d}from'../relative';
                        """
                            .trimIndent()

                    specifiers(source) shouldBe
                        listOf("react", "@/lib/util", "./side-effect", "../relative")
                }

                it("finds re-exports") {
                    val source =
                        """
                        export * from './all';
                        export * as names from './names';
                        export { a, b as c } from './some';
                        export { local };
                        export const value = 'not/an/import';
                        """
                            .trimIndent()

                    specifiers(source) shouldBe listOf("./all", "./names", "./some")
                }

                it("finds imports written without semicolons") {
                    val source =
                        """
                        import { a } from "./a"
                        export { a }
                        export { b } from "./b"
                        export { c }
                        import d from "./d"
                        """
                            .trimIndent()

                    specifiers(source) shouldBe listOf("./a", "./b", "./d")
                }

                it("finds an import once when a name in it is a keyword") {
                    val source =
                        """
                        import { import as a } from "./a";
                        export { a as import } from "./b";
                        """
                            .trimIndent()

                    specifiers(source) shouldBe listOf("./a", "./b")
                }

                it("finds dynamic imports of a literal") {
                    val source =
                        """
                        const page = await import('./page');
                        const other = import( "./other" , options);
                        const computed = import(name);
                        const meta = import.meta.url;
                        """
                            .trimIndent()

                    specifiers(source) shouldBe listOf("./page", "./other")
                }

                it("ignores text that only looks like an import") {
                    val source =
                        """
                        const text = 'Download from "/files" or import "/more"';
                        const html = `<p>import x from '/template' ${'$'}{value} from "/still-text"</p>`;
                        // import commented from '/line-comment'
                        /* export * from '/block-comment' */
                        const pattern = /import x from '\/regex'/g;
                        const record = { import: '/property', from: '/key' };
                        record.import('/method');
                        """
                            .trimIndent()

                    specifiers(source) shouldBe emptyList()
                }

                it("finds imports inside template expressions") {
                    val source = "const text = `a ${'$'}{await import('./inner')} b`;"

                    specifiers(source) shouldBe listOf("./inner")
                }

                it("tells division from a regex") {
                    val source =
                        """
                        const half = total / 2; const quote = '"'; const rest = half / 3;
                        if (ready) /"/.test(text);
                        const count = index++ / 2;
                        import './after';
                        """
                            .trimIndent()

                    specifiers(source) shouldBe listOf("./after")
                }

                it("reports where each specifier is") {
                    val source = "import a from './a'; export * from \"./b\";"
                    val found = JavaScriptImportScanner(source).scan()

                    found.map { source.substring(it.range.first, it.range.last + 1) } shouldBe
                        listOf("./a", "./b")
                }
            }
        },
    )
