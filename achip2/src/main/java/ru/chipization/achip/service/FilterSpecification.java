package ru.chipization.achip.service;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.chipization.achip.dto.search.SearchRequest;
import ru.chipization.achip.model.Animal;
import ru.chipization.achip.model.VisitedLocation;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class FilterSpecification<T> {
    public Specification<T> getSearchSpecification(List<SearchRequest> searchRequests) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            for (SearchRequest searchRequest : searchRequests) {
                Predicate equal = criteriaBuilder.like(criteriaBuilder.lower(root.get(searchRequest.getKey())), "%" + searchRequest.getValue().toString().toLowerCase() + "%");
                predicates.add(equal);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public Specification<Animal> getSearchSpecificationAnimal(List<SearchRequest> searchRequests, Boolean startTime, Boolean endTime) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            Boolean firstTime = false;
            Predicate equal = null;

            for (SearchRequest searchRequest : searchRequests) {
                if(startTime && endTime) {
                    if (searchRequest.getKey().equals("chippingDateTime") && !firstTime) {
                        Instant fff = Instant.parse(getInstantString(searchRequest.getValue().toString()));

                        firstTime = true;
                        equal = criteriaBuilder.greaterThanOrEqualTo(root.get(searchRequest.getKey()), fff);
                    } else if (searchRequest.getKey().equals("chippingDateTime") && firstTime) {
                        Instant fff = Instant.parse(getInstantString(searchRequest.getValue().toString()));

                        firstTime = false;
                        equal = criteriaBuilder.lessThanOrEqualTo(root.get(searchRequest.getKey()), fff);
                    } else {
                        if (root.get(searchRequest.getKey()).getJavaType() == String.class) {
                            equal = criteriaBuilder.like(criteriaBuilder.lower(root.get(searchRequest.getKey())), "%" + searchRequest.getValue().toString().toLowerCase() + "%");
                        } else {
                            if(searchRequest.getValue() != null) {
                                equal = criteriaBuilder.equal(root.get(searchRequest.getKey()), searchRequest.getValue());
                            } else {
                                equal = criteriaBuilder.isNull(root.get(searchRequest.getKey()));
                            }
                        }
                   }
                } else if(!startTime && endTime) {
                    if (searchRequest.getKey().equals("chippingDateTime")) {
                        Instant fff = Instant.parse(getInstantString(searchRequest.getValue().toString()));

                        firstTime = false;
                        equal = criteriaBuilder.lessThanOrEqualTo(root.get(searchRequest.getKey()), fff);
                    } else {
                        if (root.get(searchRequest.getKey()).getJavaType() == String.class) {
                            equal = criteriaBuilder.like(criteriaBuilder.lower(root.get(searchRequest.getKey())), "%" + searchRequest.getValue().toString().toLowerCase() + "%");
                        } else {
                            if(searchRequest.getValue() != null) {
                                equal = criteriaBuilder.equal(root.get(searchRequest.getKey()), searchRequest.getValue());
                            } else {
                                equal = criteriaBuilder.isNull(root.get(searchRequest.getKey()));
                            }
                        }
                    }
                } else if(startTime && !endTime) {
                    if (searchRequest.getKey().equals("chippingDateTime")) {
                        Instant fff = Instant.parse(getInstantString(searchRequest.getValue().toString()));

                        firstTime = true;
                        equal = criteriaBuilder.greaterThanOrEqualTo(root.get(searchRequest.getKey()), fff);
                    } else {
                        if (root.get(searchRequest.getKey()).getJavaType() == String.class) {
                            equal = criteriaBuilder.like(criteriaBuilder.lower(root.get(searchRequest.getKey())), "%" + searchRequest.getValue().toString().toLowerCase() + "%");
                        } else {
                            if(searchRequest.getValue() != null) {
                                equal = criteriaBuilder.equal(root.get(searchRequest.getKey()), searchRequest.getValue());
                            } else {
                                equal = criteriaBuilder.isNull(root.get(searchRequest.getKey()));
                            }
                        }
                    }
                } else if(!startTime && !endTime) {
                    if (root.get(searchRequest.getKey()).getJavaType() == String.class) {
                        equal = criteriaBuilder.like(criteriaBuilder.lower(root.get(searchRequest.getKey())), "%" + searchRequest.getValue().toString().toLowerCase() + "%");
                    } else {
                        if(searchRequest.getValue() != null) {
                            equal = criteriaBuilder.equal(root.get(searchRequest.getKey()), searchRequest.getValue());
                        } else {
                            equal = criteriaBuilder.isNull(root.get(searchRequest.getKey()));
                        }
                    }
                }

                predicates.add(equal);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public Specification<VisitedLocation> getSearchSpecificationVisitedLocation(List<SearchRequest> searchRequests, Boolean startTime, Boolean endTime) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            Boolean firstTime = false;
            Predicate equal = null;

            for (SearchRequest searchRequest : searchRequests) {
                if(startTime && endTime) {
                    if (searchRequest.getKey().equals("dateTimeOfVisitLocationPoint") && !firstTime) {
                        String offset = OffsetDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()).toString();
                        offset = offset.substring(offset.length() - 6);

                        Instant fff = Instant.parse(getInstantString(searchRequest.getValue().toString().substring(0, searchRequest.getValue().toString().length() - 1) + offset));

                        firstTime = true;
                        equal = criteriaBuilder.greaterThanOrEqualTo(root.get(searchRequest.getKey()), fff);
                    } else if (searchRequest.getKey().equals("dateTimeOfVisitLocationPoint") && firstTime) {
                        String offset = OffsetDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()).toString();
                        offset = offset.substring(offset.length() - 6);

                        Instant fff = Instant.parse(getInstantString(searchRequest.getValue().toString().substring(0, searchRequest.getValue().toString().length() - 1) + offset));

                        firstTime = false;
                        equal = criteriaBuilder.lessThanOrEqualTo(root.get(searchRequest.getKey()), fff);
                    }
                } else if(!startTime && endTime) {
                    if (searchRequest.getKey().equals("dateTimeOfVisitLocationPoint")) {
                        String offset = OffsetDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()).toString();
                        offset = offset.substring(offset.length() - 6);

                        Instant fff = Instant.parse(getInstantString(searchRequest.getValue().toString().substring(0, searchRequest.getValue().toString().length() - 1) + offset));

                        firstTime = false;
                        equal = criteriaBuilder.lessThanOrEqualTo(root.get(searchRequest.getKey()), fff);
                    }
                } else if(startTime && !endTime) {
                    if (searchRequest.getKey().equals("dateTimeOfVisitLocationPoint")) {
                        String offset = OffsetDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()).toString();
                        offset = offset.substring(offset.length() - 6);

                        Instant fff = Instant.parse(getInstantString(searchRequest.getValue().toString().substring(0, searchRequest.getValue().toString().length() - 1) + offset));

                        firstTime = true;
                        equal = criteriaBuilder.greaterThanOrEqualTo(root.get(searchRequest.getKey()), fff);
                    }
                } else if(!startTime && !endTime) {
                    continue;
                }

                predicates.add(equal);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private String getInstantString(String time) {
        String temp = time;
        if(temp.length() >= 26) {
            temp = temp.substring(0, 26) + "Z";
        }
        if(temp.indexOf('.') == -1) {
            temp = temp.substring(0, 19) + "." + temp.substring(19);
        }

        return temp;
    }
}
