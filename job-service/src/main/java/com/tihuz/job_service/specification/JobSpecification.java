package com.tihuz.job_service.specification;

import com.tihuz.job_service.entity.Job;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

public class JobSpecification {

//    public static Specification<com.tihuz.job_service.entity.Job> filter(Long userId, String keyword)
//    {
//        return(root, query, criteriaBuilder) ->
//
//        {
//            List<Predicate> predicates=new ArrayList<>();
//            if(userId!=null)
//            {
//                predicates.add(criteriaBuilder.equal(root.get("userId"),userId));
//            }
//
//            if(keyword!=null && !keyword.trim().isEmpty())
//            {
//                String likePattern="%"+keyword.toLowerCase()+"%";
//                predicates.add(criteriaBuilder
//                        .like(criteriaBuilder.lower(root.get("content")),likePattern));
//            }
//
//            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
//
//        };
//
//    }


    public static Specification<Job> filter( String keyword,String location,String experience, String category,String jobType,Long companyId,Long userId)
    {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();


            // THÊM ĐIỀU KIỆN NÀY: chỉ lấy job chưa bị xóa
            predicates.add(criteriaBuilder.isFalse(root.get("isDeleted")));

            if (userId != null)
            {
                predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
            }


            // companyId
            if (companyId != null) {
                predicates.add(criteriaBuilder.equal(root.get("companyId"), companyId));
            }



            //keyword search
            if (keyword != null && !keyword.trim().isEmpty())
            {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                Predicate titleLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), likePattern);
                Predicate descLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likePattern);
                predicates.add(criteriaBuilder.or(titleLike, descLike));
            }

            // location
            if(location!=null && !location.trim().isEmpty())
            {
              predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("location")), location.toLowerCase()));

            }

            //experience
            if(experience!=null && !experience.trim().isEmpty())
            {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("experience")), experience.toLowerCase()));
            }

            // category
            if (category != null && !category.trim().isEmpty()) {

                predicates.add(
                        criteriaBuilder.equal(
                                criteriaBuilder.lower(root.get("category")),
                                category.toLowerCase()
                        )
                );
            }

            // jobType
            if (jobType != null && !jobType.trim().isEmpty()) {

                predicates.add(
                        criteriaBuilder.equal(
                                criteriaBuilder.lower(root.get("jobType")),
                                jobType.toLowerCase()
                        )
                );
            }


            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
