package com.maosconectadas.backend.sale;

import com.maosconectadas.backend.auth.UserAccount;
import com.maosconectadas.backend.product.Product;
import com.maosconectadas.backend.product.ProductRepository;
import com.maosconectadas.backend.product.ProductVariation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class SaleService {
    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;

    public SaleService(SaleRepository saleRepository, ProductRepository productRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<SaleResponse> findAll(UserAccount owner) {
        return saleRepository.findAllByOwnerIdOrderBySoldAtDesc(owner.getId())
            .stream()
            .map(SaleResponse::from)
            .toList();
    }

    public SaleResponse create(SaleRequest request, UserAccount owner) {
        Product product = productRepository.findByIdAndOwnerIdForUpdate(request.productId(), owner.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));

        List<ProductVariation> variations = product.getVariations();
        int variationIndex = request.variationIndex();
        int available;
        String variationLabel;

        if (variations.isEmpty()) {
            if (variationIndex != 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Variação inválida");
            }
            available = product.getQuantity();
            variationLabel = "Único / " + product.getCategory();
        } else {
            if (variationIndex >= variations.size()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Variação inválida");
            }
            ProductVariation variation = variations.get(variationIndex);
            available = variation.getQuantity();
            variationLabel = "Tamanho " + variation.getSize() + " / " + variation.getColor();
        }

        if (request.quantity() > available) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Quantidade solicitada maior que o estoque disponível");
        }

        if (variations.isEmpty()) {
            product.setQuantity(available - request.quantity());
        } else {
            List<ProductVariation> updatedVariations = new ArrayList<>(variations);
            ProductVariation selected = updatedVariations.get(variationIndex);
            updatedVariations.set(variationIndex, new ProductVariation(
                selected.getSize(), selected.getColor(), available - request.quantity()));
            product.setVariations(updatedVariations);
            product.setQuantity(updatedVariations.stream()
                .mapToInt(ProductVariation::getQuantity)
                .sum());
        }
        productRepository.save(product);

        BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(request.quantity()));
        Sale sale = saleRepository.save(new Sale(
            product.getName(),
            variationLabel,
            request.quantity(),
            product.getPrice(),
            totalPrice,
            LocalDateTime.now(),
            owner));
        return SaleResponse.from(sale);
    }
}
