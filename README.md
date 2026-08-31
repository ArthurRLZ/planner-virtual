# 📅 Planner Virtual

Um sistema moderno de planejamento pessoal e acadêmico desenvolvido em **Kotlin Multiplatform** com **Compose Multiplatform** voltado para WebAssembly (**Wasm/JS**). O Planner Virtual ajuda estudantes e profissionais a organizarem suas rotinas, acompanharem metas de curto e longo prazo, gerenciarem tarefas diárias com alocação em blocos de tempo e monitorarem seu rendimento por meio de relatórios analíticos.

---

## 🛠️ Tecnologias e Stack Utilizada

- **Linguagem:** Kotlin 2.1.20
- **Framework UI:** Compose Multiplatform 1.7.3 (Material 3)
- **Target:** WebAssembly / Browser (`wasmJs`)
- **Persistência:** LocalStorage do navegador (via `kotlinx.browser` e `kotlinx.serialization`)
- **Manipulação de Datas:** `kotlinx-datetime`
- **Build Tool:** Gradle com Kotlin DSL (`build.gradle.kts`)

---

## ✨ Principais Funcionalidades

### 1. 📊 Painel Analítico (Dashboard)
- Resumo consolidado do dia selecionado (tarefas pendentes, concluídas, metas em andamento e próximos lembretes).
- Indicador visual da porcentagem de produtividade diária.
- Calendário mensal interativo com indicadores visuais que destacam dias contendo tarefas, metas ou lembretes.

### 2. ✅ Gestão de Tarefas
- Planejamento de tarefas diárias categorizadas (Faculdade, Trabalho, Saúde, Lazer, Projetos Pessoais, Estudos).
- Suporte a alocação por **Blocos de Tempo** (30 minutos, 1 hora ou por Turno: Manhã, Tarde, Noite).
- Níveis de prioridade (Baixa, Média, Alta).
- Gestão completa de ciclo de vida e status (Pendente, Executada, Parcialmente Executada, Cancelada, Adiada).
- Destaque cromático por categoria e navegação rápida por datas.

### 3. 🎯 Planejamento de Metas
- Organização de objetivos por horizonte de tempo (**Semanal**, **Mensal**, **Anual**).
- Acompanhamento de progresso geral e cálculo automático de períodos de vigência.
- Gestão de status de cumprimento (Cumprida, Parcialmente Cumprida, Não Cumprida).
- Seções colapsáveis por período e estados vazios acionáveis para cadastro rápido.

### 4. ⏰ Lembretes
- Cadastro de lembretes categorizados por tipo (Reunião, Ligação, Compra, Estudo, Exercício, Entrega de Trabalho).
- Suporte a recorrência **Única** ou **Semanal** (com seleção de dia da semana).
- Listagem completa e exclusão com diálogo de confirmação e feedback flutuante via Snackbar.

### 5. 📈 Relatórios de Desempenho
- Geração de relatórios periódicos (**Semanais**, **Mensais** e **Anuais**).
- Métricas detalhadas de taxa de conclusão de tarefas e cumprimento de metas.
- Distribuição de atividades por categoria e acompanhamento de produtividade.

### 6. 🎓 Integração com Google Classroom
- Sincronização e importação de entregas e trabalhos acadêmicos diretamente para o fluxo de tarefas do planner.

---

## 🏗️ Arquitetura e Estrutura de Pastas

O projeto adota uma arquitetura em camadas com separação clara de responsabilidades:

```text
src/wasmJsMain/kotlin/
├── data/
│   └── repository/        # Implementações de repositórios (LocalStorage e EmMemória)
├── domain/
│   ├── model/             # Entidades de negócio, DTOs e Enums (Tarefa, Meta, Lembrete, etc.)
│   ├── repository/        # Interfaces e contratos dos repositórios
│   └── usecase/           # Regras de negócio e casos de uso organizados por módulo:
│       ├── classroom/     # Sincronização com Classroom
│       ├── lembrete/      # Criar, Listar e Remover lembretes
│       ├── meta/          # Criar, Listar, Atualizar status e Remover metas
│       ├── painel/        # Resumo do dia e atividades do mês
│       ├── relatorio/     # Geração de relatórios semanais, mensais e anuais
│       └── tarefa/        # Criar, Listar por data, Atualizar status e Remover tarefas
├── facade/                # Facade central do Planner
├── ui/
│   ├── screens/           # Telas do Compose (Painel, Tarefas, Metas, Lembretes, Classroom, Relatórios)
│   ├── theme/             # Cores centralizadas por categoria e estilos visuais
│   └── App.kt             # Navegação principal (Drawer) e injeção de dependências dos use cases
└── util/                  # Utilitários de data e geradores de identificadores únicos
```

---

## 🚀 Como Executar o Projeto Localmente

### Pré-requisitos
- **JDK 17** ou superior instalado e configurado nas variáveis de ambiente (`JAVA_HOME`).
- Navegador moderno com suporte a WebAssembly (Google Chrome, Microsoft Edge, Firefox, etc.).

### 1. Clonar o repositório
```bash
git clone https://github.com/PLP-Project-UFAPE/planner-virtual.git
cd planner-virtual
```

### 2. Compilar o projeto
No Windows (PowerShell / CMD):
```powershell
.\gradlew.bat build
```
No Linux / macOS:
```bash
./gradlew build
```

### 3. Executar o servidor de desenvolvimento
No Windows (PowerShell / CMD):
```powershell
.\gradlew.bat wasmJsBrowserDevelopmentRun
```
No Linux / macOS:
```bash
./gradlew wasmJsBrowserDevelopmentRun
```

Após compilar, acesse o endereço no navegador:
```text
http://localhost:8080/
```

---

## 👥 Integrantes do Grupo

- **Arthur Passos** - [@ArthurRLZ](https://github.com/ArthurRLZ)
- **Augusto Jorge** - [@AugustoJBM](https://github.com/AugustoJBM)
- **Euclides Laurindo** - [@euclideslaurindo](https://github.com/euclideslaurindo)
- **Heitor Calado** - [@HeitorCalado](https://github.com/HeitorCalado)
- **Joaci Laurindo** - [@joacif](https://github.com/joacif)
- **João Vitor** - [@Joaovitorrr07](https://github.com/Joaovitorrr07)
- **Luís Arthur** - [@lu1s-4rthur](https://github.com/lu1s-4rthur)
