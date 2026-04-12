// Pre-define Expo globals to prevent the lazy-loader in
// expo/src/winter/runtime.native from triggering cross-scope
// imports within the Jest sandbox.

if (typeof globalThis.__ExpoImportMetaRegistry === 'undefined') {
  globalThis.__ExpoImportMetaRegistry = new Map();
}

// Ensure structuredClone exists (Node >= 17 has it natively)
if (typeof globalThis.structuredClone === 'undefined') {
  globalThis.structuredClone = (val) => JSON.parse(JSON.stringify(val));
}
