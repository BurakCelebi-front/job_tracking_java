package com.example.job_tracking.entity;

    import jakarta.persistence.Entity;
    import jakarta.persistence.FetchType;
    import jakarta.persistence.GeneratedValue;
    import jakarta.persistence.GenerationType;
    import jakarta.persistence.Id;
    import jakarta.persistence.JoinColumn;
    import jakarta.persistence.ManyToOne;
    import lombok.Getter;
    import lombok.Setter;
    import lombok.NoArgsConstructor;

    @Entity
    @Getter
    @Setter
    @NoArgsConstructor
    public class Company{
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;
        private String website;
        private String location;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "owner_id")
        private User owner;
    }

