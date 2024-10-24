import { StyleObject } from "@inductiveautomation/perspective-client";
import uniqueClasses from "../uniqueClasses";

/**
 * Merges Perspective `StyleObjects`.
 * CSS properties are combined, and classes are joined and deduplicated.
 * @param styles 
 * @returns 
 */
export default function mergeStyles(styles: StyleObject[]): StyleObject {

    const mergedClasses = uniqueClasses(styles
        .map((s) => s?.classes)
        .filter((s) => s !== undefined)
        .flatMap((s) => Array.isArray(s) ? s : s.split(' ')))

    return {
        ...Object.assign({}, ...styles),
        classes: mergedClasses.length > 0 ? mergedClasses : undefined
    }
  }