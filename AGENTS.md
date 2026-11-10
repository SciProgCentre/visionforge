## General guidelines

* Don't edit README.md files, edit README-TEMPLATE.md in the docs directory for each module instead. Readme files are generated from templates.
* Mark all large fragments (more than 20 lines) of generated code and tests with a comment including "LLM generated code" and a short description of the generation task.
* Update unreleased sections in CHANGELOG.md files on all significant changes.

## Specific code guidelines

* When converting string constant to a DataForge Name, use `parseAsName()` if name string has several tokens separated by dots.
* When creating tests, ensure that a minimal number of tests is used to cover functionality and remove code duplication if possible to avoid increasing code size and build time.
* When adding new features or changing the public behavior of existing ones, update Markdown docs in a relevant module `docs` directory.
* Keep documentation concise and simple. Introduce examples but keep implementation details to a minimum.