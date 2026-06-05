package dk.sunepoulsen.tes.features.service.domains.persistence.model;

import dk.sunepoulsen.tes.jpa.model.TimestampEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Builder
@Data
@ToString(exclude = {"activations"})
@EqualsAndHashCode(exclude = {"activations"}, callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "features", name = "features")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class FeatureEntity extends TimestampEntity {

    private static final String SEQUENCE_NAME = "feature_groups_id_seq";

    /**
     * Primary key.
     */
    @Id
    @SequenceGenerator(schema = "features", name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "feature_group_id", nullable = false)
    private FeatureGroupEntity featureGroup;

    @Column(name = "feature_key", nullable = false, unique = true)
    private String key;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @OneToMany(mappedBy = "feature", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<FeatureActivationEntity> activations;
}
