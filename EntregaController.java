package br.com.escola.entregas;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/entregas")
public class EntregaController {
    private final RestClient client = RestClient.create();
    private final List<Entrega> entregas = new ArrayList<>();
    private final AtomicInteger proximoId = new AtomicInteger(1);

    @Value("${servicos.produtos.url:http://localhost:8081}")
    private String produtosUrl;

    @Value("${servicos.pedidos.url:http://localhost:8082}")
    private String pedidosUrl;

    @GetMapping
    public List<Entrega> listar() {
        return entregas;
    }

    @GetMapping("/{id}")
    public Entrega buscar(@PathVariable int id) {
        return entregas.stream()
            .filter(entrega -> entrega.id() == id)
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrega não encontrada"));
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody NovaEntrega nova) {
        try {
            Produto produto = client.get()
                .uri(produtosUrl + "/produtos/" + nova.produtoId())
                .retrieve().body(Produto.class);

            Pedido pedido = client.get()
                .uri(pedidosUrl + "/pedidos/" + nova.pedidoId())
                .retrieve().body(Pedido.class);

            if (produto == null || pedido == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto ou pedido inválido");
            }
            if (pedido.produtoId() != nova.produtoId()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O produto não corresponde ao pedido");
            }

            Entrega entrega = new Entrega(
                proximoId.getAndIncrement(), nova.pedidoId(), nova.produtoId(),
                nova.endereco(), "AGUARDANDO_ENVIO"
            );
            entregas.add(entrega);
            return ResponseEntity.status(HttpStatus.CREATED).body(entrega);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Não foi possível validar o produto e o pedido. Confira se os serviços estão ligados."
            );
        }
    }

    public record NovaEntrega(int pedidoId, int produtoId, String endereco) {}
    public record Entrega(int id, int pedidoId, int produtoId, String endereco, String status) {}
    public record Produto(int id, String nome, double preco) {}
    public record Pedido(int id, int produtoId, int quantidade, String status) {}
}
