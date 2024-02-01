package com.ecommerce.productservice.util;

import com.ecommerce.productservice.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;

@Configuration
@RequiredArgsConstructor
public class ProductModelListener extends AbstractMongoEventListener<Product> {

	private final MongoSequenceGenerator sequenceGenerator;

	@Override
	public void onBeforeConvert(BeforeConvertEvent<Product> event) {

		event.getSource().setProductId(sequenceGenerator.generateSequence(Product.SEQUENCE_NAME));
	}

}
