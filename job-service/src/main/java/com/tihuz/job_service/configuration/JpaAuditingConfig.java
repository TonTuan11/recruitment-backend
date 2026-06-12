    package com.tihuz.job_service.configuration;

    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.data.domain.AuditorAware;
    import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.context.SecurityContextHolder;
    import java.util.Optional;

    //This class is used by @CreatedBy and @LastModifiedBy to get the current user.

    @Configuration
    @EnableJpaAuditing(auditorAwareRef = "auditorProvider")  // enable auditing and select the bean to get the current user.
    public class JpaAuditingConfig {

        @Bean
        public AuditorAware<String> auditorProvider()
        {
            return () ->
            {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth == null || !auth.isAuthenticated())
                {
                    return Optional.of("SYSTEM");
                }
                return Optional.of(auth.getName());
            };
        }
    }