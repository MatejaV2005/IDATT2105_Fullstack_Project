import pluginVue from 'eslint-plugin-vue'
import tseslint from 'typescript-eslint'
import vueParser from 'vue-eslint-parser'
import globals from 'globals'

export default [
  {
    ignores: ['dist/**', 'coverage/**'],
  },
  ...tseslint.configs.recommended,
  ...pluginVue.configs['flat/recommended'],
  {
    files: ['**/*.{ts,tsx,vue}'],
    rules: {
      // Add or override rules here
    },
    languageOptions: {
      parser: vueParser,
      parserOptions: {
        parser: tseslint.parser,
        sourceType: 'module',
        ecmaVersion: 'latest',
        extraFileExtensions: ['.vue'],
      },
      sourceType: 'module',
      globals: {
        ...globals.browser,
      },
    },
  },
]
