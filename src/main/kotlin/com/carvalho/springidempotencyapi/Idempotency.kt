package com.carvalho.springidempotencyapi

class Idempotency {


    fun execute() {
        // teve erro na hora de gerar a chave? joga um erro com mensagem e status que vem do cliente

        // tenta inserir a chave no banco, caso dê erro
        //   se o erro nao for de chave duplicada não faz nada, retorna
        //   se existir o registro e não tiver response, joga erro pois provavelmente foi algum cenário de concorrencia (joga um erro com mensagem e status que vem do cliente)
        //   se existir o registro e a request for diferent da request existente, joga erro de idempotencia (joga um erro com mensagem e status que vem do cliente)
        //   retorna o response que está no banco
        // tenta inserir a chave no banco, caso não de erro continua
        // continua a requisicao
        //   se der alguma excecao, remove o registro inserido no banco e joga a excecao
        //   se a resposta da filtragem foi algo diferente de 2xx remove o registro inserido no banco
        //   se a resposta da filtragem foi 2xx atualiza o registro no banco com a resposta



    }


}
