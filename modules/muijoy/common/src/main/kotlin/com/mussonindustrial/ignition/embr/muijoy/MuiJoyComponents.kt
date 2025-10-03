package com.mussonindustrial.ignition.embr.muijoy

import com.inductiveautomation.perspective.common.api.BrowserResource
import com.mussonindustrial.ignition.embr.muijoy.Meta.SHORT_MODULE_ID
import com.mussonindustrial.ignition.embr.muijoy.component.Slot
import com.mussonindustrial.ignition.embr.muijoy.component.input.Button
import com.mussonindustrial.ignition.embr.muijoy.component.input.ButtonGroup
import com.mussonindustrial.ignition.embr.muijoy.component.input.Checkbox
import com.mussonindustrial.ignition.embr.muijoy.component.input.FormControl
import com.mussonindustrial.ignition.embr.muijoy.component.input.FormHelperText
import com.mussonindustrial.ignition.embr.muijoy.component.input.FormLabel
import com.mussonindustrial.ignition.embr.muijoy.component.input.Input
import com.mussonindustrial.ignition.embr.muijoy.component.input.Radio
import com.mussonindustrial.ignition.embr.muijoy.component.input.RadioGroup
import com.mussonindustrial.ignition.embr.muijoy.component.input.Slider
import com.mussonindustrial.ignition.embr.muijoy.component.nav.Dropdown
import com.mussonindustrial.ignition.embr.muijoy.component.nav.Menu
import com.mussonindustrial.ignition.embr.muijoy.component.nav.MenuButton
import com.mussonindustrial.ignition.embr.muijoy.component.nav.MenuItem

object MuiJoyComponents {

    private val JS_RESOURCE =
        BrowserResource(
            "embr-muijoy-client-js",
            "/res/${SHORT_MODULE_ID}/embr-muijoy-client.js",
            BrowserResource.ResourceType.JS,
        )

    private val CSS_RESOURCE =
        BrowserResource(
            "embr-muijoy-css",
            "/res/${SHORT_MODULE_ID}/embr-muijoy.css",
            BrowserResource.ResourceType.CSS,
        )
    val BROWSER_RESOURCES = mutableSetOf(JS_RESOURCE, CSS_RESOURCE)
    val REQUIRED_RESOURCES = mutableSetOf(JS_RESOURCE, CSS_RESOURCE)

    val components =
        listOf(
            Button,
            ButtonGroup,
            Checkbox,
            Dropdown,
            FormControl,
            FormHelperText,
            FormLabel,
            Input,
            Menu,
            MenuButton,
            MenuItem,
            Radio,
            RadioGroup,
            Slider,
            Slot,
        )
}
