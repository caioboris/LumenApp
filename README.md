# LumenApp - Cadastro e Listagem de Painéis Solares
O LumenApp é um aplicativo Android responsável por cadastrar e listar painéis solares de uma residência, juntamente com a produção média de energia elétrica gerada por cada painel.

## Funcionalidades
Cadastro de Painéis: O usuário pode cadastrar novos painéis solares, informando o nome do painel e sua produção média (em kWh).
Listagem de Painéis: O usuário pode visualizar uma lista com os painéis cadastrados, contendo informações como ID, nome e produção média.
Edição de Painéis: O usuário pode editar os dados de um painel solar existente.
Exclusão de Painéis: O usuário pode excluir um painel solar da lista.

## Tecnologias Usadas
Android: Utiliza o Jetpack Compose para a criação da interface do usuário de maneira declarativa.
OkHttp: Biblioteca para fazer requisições HTTP assíncronas.
Gson: Biblioteca para serializar e desserializar objetos Java para JSON e vice-versa.
Coroutines: Utilizadas para gerenciamento de tarefas assíncronas.
MVVM: Arquitetura Model-View-ViewModel (MVVM) foi adotada para separar a lógica de negócios da interface de usuário.

## Endpoints da API
A comunicação com o servidor é feita através de requisições HTTP para os seguintes endpoints:

GET /api/Painel: Retorna uma lista de todos os painéis cadastrados.
POST /api/Painel: Cria um novo painel com os dados fornecidos.
PUT /api/Painel/{id}: Atualiza os dados de um painel existente com o ID fornecido.
DELETE /api/Painel/{id}: Exclui um painel com o ID fornecido.

## Como Usar
1. Instalar o App
   Clone este repositório no seu ambiente local.
   Abra o projeto no Android Studio.
   Execute o aplicativo no seu dispositivo Android ou no emulador.
2. Acessar com o usuário:
   *admin*
   *1234*
3. Cadastrar um Painel
   Ao abrir o aplicativo, na tela de Cadastro de Painel, insira o nome do painel e a produção média de energia.
   Clique no botão Cadastrar.
   Uma vez que o cadastro seja realizado com sucesso, uma mensagem será exibida e o usuário será redirecionado para a tela anterior.
3. Visualizar a Listagem de Painéis
   Após cadastrar pelo menos um painel, a tela de Listagem de Painéis exibirá todos os painéis cadastrados com suas respectivas informações (ID, nome e produção média).
   O usuário pode editar ou excluir os painéis da lista clicando nos ícones de Editar ou Excluir.
4. Editar ou Excluir um Painel
   Para editar um painel, clique no ícone de editar (ícone de lápis) ao lado de um painel na lista.
   Para excluir um painel, clique no ícone de excluir (ícone de lixeira) ao lado do painel que deseja remover. O painel será excluído da lista e a interface será atualizada.

## Exemplo de Uso
   Cadastrar um painel:

Nome: "Painel Solar 1"
Produção Média: "5.0 kWh"

Listagem:

ID: 1
Nome: "Painel Solar 1"
Produção Média: "5.0 kWh"
Edição de Painel:

Nome: "Painel Solar 1" → "Painel Solar 1 (Atualizado)"
Produção Média: "5.0 kWh" → "6.0 kWh"