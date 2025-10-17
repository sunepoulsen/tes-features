package dk.sunepoulsen.tes.features.service.domains.persistence.model;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "features", name = "feature_groups_activations")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class FeatureGroupActivationEntity extends ActivationEntity {

    private static final String SEQUENCE_NAME = "feature_group_activation_id_seq";

    /**
     * Primary key.
     */
    @Id
    @SequenceGenerator(schema = "features", name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    @Column(name = "id")
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "feature_group_id", nullable = false)
    private FeatureGroupEntity featureGroup;

}
