import {useMediaQuery} from "@vueuse/core";
import type {SortField} from "./sdk/entities";

export type SortDefinition = { sort?: SortField; reverse?: boolean };
export const useMobileMediaQuery = () => useMediaQuery("(max-width: 576px)");
export const useCompactMediaQuery = () => useMediaQuery("(max-width: 768px)");
export const useLessHeightQuery = () => useMediaQuery("(max-height: 720px)");
export const useMoreHeightQuery = () => useMediaQuery("(min-height: 810px)");
