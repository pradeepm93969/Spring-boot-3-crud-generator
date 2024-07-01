package com.pradeep.crudgenerator.datamodal;

//import jakarta.persistence.criteria.CriteriaBuilder;
//import jakarta.persistence.criteria.Predicate;
//import jakarta.persistence.criteria.Root;
//import org.springframework.data.jpa.domain.Specification;

public class RsqlParser {

    /**public static <T> Specification<T> getSpecification(String rsql) {
        return (root, query, criteriaBuilder) -> {
            return buildSpecification(root, criteriaBuilder, rsql);
        };
    }

    private static <T> Predicate buildSpecification(
            Root<T> root,
            CriteriaBuilder criteriaBuilder,
            String rsql) {

        // Split the RSQL query into individual expressions
        String[] andExpressions = rsql.split("\\bor\\b");
        Predicate[] orPredicates = new Predicate[andExpressions.length];

        for (int i = 0; i < andExpressions.length; i++) {
            String[] expressions = andExpressions[i].split("\\band\\b");
            Predicate[] andPredicates = new Predicate[expressions.length];

            for (int j = 0; j < expressions.length; j++) {
                andPredicates[j] = buildExpression(root, criteriaBuilder, expressions[j].trim());
            }

            orPredicates[i] = criteriaBuilder.and(andPredicates);
        }

        return criteriaBuilder.or(orPredicates);
    }

    private static <T> Predicate buildExpression(
            Root<T> root,
            CriteriaBuilder criteriaBuilder,
            String expression) {

        // Split the expression into field, operator, and value
        String[] parts = expression.split("(?i)\\b(=eq=|=eqic=|=ne=|=neic=|=null=|=lk=|=le=|=ge=|=lt=|=gt=|=bw=|=in=|=out=)\\b", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid RSQL expression: " + expression);
        }

        String field = parts[0].trim();
        String operator = parts[1].trim();
        String value = parts[1].trim();

        // Build the predicate based on the operator
        switch (operator) {
            case "=eq=":
                return criteriaBuilder.equal(root.get(field), value);
            case "=eqic=":
                return criteriaBuilder.equal(criteriaBuilder.lower(root.get(field)), value.toLowerCase());
            case "=ne=":
                return criteriaBuilder.notEqual(root.get(field), value);
            case "=neic=":
                return criteriaBuilder.notEqual(criteriaBuilder.lower(root.get(field)), value.toLowerCase());
            case "=null=":
                return criteriaBuilder.isNull(root.get(field));
            case "=lk=":
                return criteriaBuilder.like(root.get(field), value);
            case "=gt=":
                return criteriaBuilder.greaterThan(root.get(field), value);
            case "=ge=":
                return criteriaBuilder.greaterThanOrEqualTo(root.get(field), value);
            case "=lt=":
                return criteriaBuilder.lessThan(root.get(field), value);
            case "=le=":
                return criteriaBuilder.lessThanOrEqualTo(root.get(field), value);
            case "=bw=":
                String[] rangeValues = value.split(",");
                if (rangeValues.length != 2) {
                    throw new IllegalArgumentException("Invalid range value for BETWEEN operator: " + value);
                }
                return criteriaBuilder.between(root.get(field), rangeValues[0], rangeValues[1]);
            case "=in=":
                String[] inValues = value.split(",");
                return root.get(field).in((Object[]) inValues);
            case "=out=":
                String[] notInValues = value.split(",");
                return criteriaBuilder.not(root.get(field).in((Object[]) notInValues));
            default:
                throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
    }**/
}

