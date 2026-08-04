# Build & Release

## Conta do GitHub CLI

Esta máquina tem mais de uma conta logada no `gh`. Releases e pushes deste repo devem ser feitos
com a conta `josepacelli`, não com outra conta que esteja ativa no momento.

Verificar qual conta está ativa:
```sh
gh auth status
```

Trocar para `josepacelli`:
```sh
gh auth switch --hostname github.com --user josepacelli
```
