package net.biomodels.jummp.core.model.identifier.decorator

interface OrderedModelIdentifierDecorator extends ModelIdentifierDecorator,
            Comparable<OrderedModelIdentifierDecorator> {
    final short ORDER
}
