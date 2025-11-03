/* OwlPlug
 * Copyright (C) 2021 Arthur <dropsnorz@gmail.com>
 *
 * This file is part of OwlPlug.
 *
 * OwlPlug is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * OwlPlug is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OwlPlug.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.owlplug.explore.ui;

import com.owlplug.controls.ChipView;
import com.owlplug.controls.DefaultChip;
import com.owlplug.core.components.ApplicationDefaults;
import com.owlplug.explore.model.search.ExploreFilterCriteria;
import com.owlplug.explore.model.search.ExploreFilterCriteriaType;
import javafx.collections.ListChangeListener;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.util.StringConverter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.owlplug.explore.model.search.ExploreFilterCriteriaType.CREATOR;
import static com.owlplug.explore.model.search.ExploreFilterCriteriaType.TAG;
import static com.owlplug.plugin.model.PluginType.EFFECT;
import static com.owlplug.plugin.model.PluginType.INSTRUMENT;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;

public class ExploreChipView extends ChipView<ExploreFilterCriteria> {

    private final ApplicationDefaults applicationDefaults;
    private final List<String> pluginCreators;

    private static final String PROMPT_TEXT = "Enter your search query by Name, Authors, Category...";

    /**
     * Creates an ExploreChipView.
     *
     * @param applicationDefaults - OwlPlug application defaults
     */
    public ExploreChipView(ApplicationDefaults applicationDefaults, List<String> pluginCreators) {
        super();
        this.applicationDefaults = applicationDefaults;
        this.pluginCreators = pluginCreators;
        this.setPromptText(PROMPT_TEXT);
        init();
    }

    private void init() {

        Map<String, ExploreFilterCriteria> suggestions = new LinkedHashMap<>();

        suggestions.put("Effect", new ExploreFilterCriteria(EFFECT, ExploreFilterCriteriaType.TYPE, applicationDefaults.effectImage, "Effect"));
        suggestions.put("Instrument", new ExploreFilterCriteria(INSTRUMENT, ExploreFilterCriteriaType.TYPE, applicationDefaults.instrumentImage, "Instrument"));
        suggestions.put("Amp", new ExploreFilterCriteria("Amp", TAG, applicationDefaults.tagImage));
        suggestions.put("Analog", new ExploreFilterCriteria("Analog", TAG, applicationDefaults.tagImage));
        suggestions.put("Ambient", new ExploreFilterCriteria("Ambient", TAG, applicationDefaults.tagImage));
        suggestions.put("Bass", new ExploreFilterCriteria("Bass", TAG, applicationDefaults.tagImage));
        suggestions.put("Brass", new ExploreFilterCriteria("Brass", TAG, applicationDefaults.tagImage));
        suggestions.put("Compressor", new ExploreFilterCriteria("Compressor", TAG, applicationDefaults.tagImage));
        suggestions.put("Delay", new ExploreFilterCriteria("Delay", TAG, applicationDefaults.tagImage));
        suggestions.put("Distortion", new ExploreFilterCriteria("Distortion", TAG, applicationDefaults.tagImage));
        suggestions.put("Drum", new ExploreFilterCriteria("Drum", TAG, applicationDefaults.tagImage));
        suggestions.put("Equalizer", new ExploreFilterCriteria("Equalizer", TAG, applicationDefaults.tagImage));
        suggestions.put("Filter", new ExploreFilterCriteria("Filter", TAG, applicationDefaults.tagImage));
        suggestions.put("Flanger", new ExploreFilterCriteria("Flanger", TAG, applicationDefaults.tagImage));
        suggestions.put("Gate", new ExploreFilterCriteria("Gate", TAG, applicationDefaults.tagImage));
        suggestions.put("Guitar", new ExploreFilterCriteria("Guitar", TAG, applicationDefaults.tagImage));
        suggestions.put("LFO", new ExploreFilterCriteria("LFO", TAG, applicationDefaults.tagImage));
        suggestions.put("Limiter", new ExploreFilterCriteria("Limiter", TAG, applicationDefaults.tagImage));
        suggestions.put("Maximizer", new ExploreFilterCriteria("Maximizer", TAG, applicationDefaults.tagImage));
        suggestions.put("Monophonic", new ExploreFilterCriteria("Monophonic", TAG, applicationDefaults.tagImage));
        suggestions.put("Orchestral", new ExploreFilterCriteria("Orchestral", TAG, applicationDefaults.tagImage));
        suggestions.put("Organ", new ExploreFilterCriteria("Organ", TAG, applicationDefaults.tagImage));
        suggestions.put("Panner", new ExploreFilterCriteria("Panner", TAG, applicationDefaults.tagImage));
        suggestions.put("Phaser", new ExploreFilterCriteria("Phaser", TAG, applicationDefaults.tagImage));
        suggestions.put("Piano", new ExploreFilterCriteria("Piano", TAG, applicationDefaults.tagImage));
        suggestions.put("Reverb", new ExploreFilterCriteria("Reverb", TAG, applicationDefaults.tagImage));
        suggestions.put("Tremolo", new ExploreFilterCriteria("Tremolo", TAG, applicationDefaults.tagImage));
        suggestions.put("Tube", new ExploreFilterCriteria("Tube", TAG, applicationDefaults.tagImage));
        suggestions.put("Synth", new ExploreFilterCriteria("Synth", TAG, applicationDefaults.tagImage));
        suggestions.put("Vintage", new ExploreFilterCriteria("Vintage", TAG, applicationDefaults.tagImage));

        pluginCreators.forEach(creator -> suggestions.put(creator, new ExploreFilterCriteria(creator, CREATOR, applicationDefaults.userImage)));

        getSuggestions().addAll(suggestions.values());
        setConverter(new StringConverter<>() {
            @Override
            public String toString(ExploreFilterCriteria object) {
                return object.toString();
            }

            @Override
            public ExploreFilterCriteria fromString(final String value) {
                String filter = value.trim();
                ExploreFilterCriteria found = suggestions.get(filter);
                return found == null ? new ExploreFilterCriteria(filter, ExploreFilterCriteriaType.NAME) : found;
            }
        });

        setChipFactory((chipView, criteria) -> new DefaultChip<>(chipView, criteria) {
            {
                if (getItem().getFilterType() == ExploreFilterCriteriaType.TYPE) {
                    root.getStyleClass().add("chip-brown");
                }
                if (getItem().getFilterType() == TAG) {
                    root.getStyleClass().add("chip-red");
                }
                if (getItem().getFilterType() == CREATOR) {
                    root.getStyleClass().add("chip-blue");
                }
            }
        });

        getChips().addListener((ListChangeListener<ExploreFilterCriteria>) change -> {
            // Only display prompt text if any chips is selected
            if (isEmpty(getChips())) {
                setPromptText(PROMPT_TEXT);
                return;
            }
            setPromptText(EMPTY);
        });

        setSuggestionsCellFactory(param -> new ListCell<>() {
            protected void updateItem(ExploreFilterCriteria item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setText(item.toString());
                    ImageView imageView = new ImageView(item.getIcon());
                    imageView.setFitWidth(10);
                    imageView.setFitHeight(10);
                    setGraphic(imageView);
                } else {
                    setGraphic(null);
                    setText(null);
                }
            }
        });

    }

}
