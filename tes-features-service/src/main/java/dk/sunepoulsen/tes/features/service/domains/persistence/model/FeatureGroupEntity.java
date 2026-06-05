package dk.sunepoulsen.tes.features.service.domains.persistence.model;

import dk.sunepoulsen.tes.jpa.model.TimestampEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;


/**
 * Represents a feature group entity in the persistence layer.
 * This entity is stored in the "feature_groups" table under the "features" schema.
 * It inherits from {@link TimestampEntity} to include common timestamp-related fields.
 * <p>
 * This class provides the following properties:
 * - A unique identifier for the feature group.
 * - A key to uniquely identify the group by a business or logical name.
 * - A name that describes the feature group.
 * - A description that provides additional details about the feature group.
 * - A list of features associated with the group.
 * - A list of activations controlling the group's enabled state.
 * <p>
 * Annotations are used for database mapping and object-to-relational mapping,
 * as well as for object utility purposes provided by Lombok.
 * <p>
 * The following relationships are defined:
 * - One-to-many with {@link FeatureEntity}, where the features are grouped by this entity.
 * - One-to-many with {@link FeatureGroupActivationEntity}, which tracks activation states for the group.
 */
@Builder
@Data
@ToString(exclude = {"activations", "features"})
@EqualsAndHashCode(exclude = {"activations", "features"}, callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(schema = "features", name = "feature_groups")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class FeatureGroupEntity extends TimestampEntity {

    private static final String SEQUENCE_NAME = "feature_groups_id_seq";

    @Id
    @SequenceGenerator(schema = "features", name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    @Column(name = "id")
    private Long id;

    @Column(name = "group_key", nullable = false)
    private String key;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @OneToMany(mappedBy = "featureGroup", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<FeatureEntity> features;

    @OneToMany(mappedBy = "featureGroup", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<FeatureGroupActivationEntity> activations;

}
