package truonggg.strategy.impl;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import truonggg.Enum.PaymentMethod;
import truonggg.strategy.PaymentStrategy;

@Component
public class PaymentStrategyFactory {

	private final Map<PaymentMethod, PaymentStrategy> strategyMap;

	public PaymentStrategyFactory(List<PaymentStrategy> strategies) {
		strategyMap = strategies.stream()
				.collect(Collectors.toMap(PaymentStrategy::getSupportedMethod, Function.identity()));
	}

	public PaymentStrategy getStrategy(PaymentMethod method) {
		PaymentStrategy strategy = strategyMap.get(method);
		if (strategy == null) {
			throw new IllegalArgumentException("Unsupported payment method");
		}
		return strategy;
	}
}
