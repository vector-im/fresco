/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.fresco.vito.core.impl

import android.graphics.Canvas
import android.graphics.Rect
import com.facebook.fresco.vito.options.BorderOptions
import com.facebook.fresco.vito.options.RoundingOptions
import com.facebook.fresco.vito.renderer.CanvasTransformation
import com.facebook.fresco.vito.renderer.CanvasTransformationHandler
import com.facebook.fresco.vito.renderer.ImageDataModel
import com.facebook.fresco.vito.renderer.RenderCommand

class ImageLayerDataModel {
  private var dataModel: ImageDataModel? = null
  private var roundingOptions: RoundingOptions? = null
  private var borderOptions: BorderOptions? = null
  private var currentBounds: Rect? = null
  private val canvasTransformationHandler: CanvasTransformationHandler =
      CanvasTransformationHandler(null)
  private var renderCommand: RenderCommand? = null

  fun getDataModel(): ImageDataModel? {
    return dataModel
  }

  fun configure(
      dataModel: ImageDataModel? = this.dataModel,
      roundingOptions: RoundingOptions? = this.roundingOptions,
      borderOptions: BorderOptions? = this.borderOptions,
      canvasTransformation: CanvasTransformation? =
          canvasTransformationHandler.canvasTransformation,
      bounds: Rect? = this.currentBounds
  ) {
    this.dataModel = dataModel
    this.roundingOptions = roundingOptions
    this.borderOptions = borderOptions
    this.currentBounds = bounds
    canvasTransformationHandler.canvasTransformation = canvasTransformation
    // TODO(T105148151): only invalidate if changed
    invalidateRenderCommand()
    if (bounds != null) {
      computeRenderCommand(bounds)
    }
  }

  fun invalidateRenderCommand() {
    renderCommand = null
  }

  private fun computeRenderCommand(bounds: Rect, alpha: Int = 255) {
    val model = dataModel
    if (model == null) {
      renderCommand = null
      return
    }
    if (renderCommand != null && currentBounds == bounds) {
      return
    }
    currentBounds = bounds
    canvasTransformationHandler.configure(bounds, model.width, model.height)
    renderCommand =
        ImageWithTransformationAndBorderRenderer.createRenderCommand(
            model,
            roundingOptions,
            borderOptions,
            canvasTransformationHandler.getMatrix(),
            bounds,
            alpha)
  }

  fun draw(canvas: Canvas) {
    renderCommand?.let { it(canvas) }
  }

  fun reset() {
    canvasTransformationHandler.canvasTransformation = null
    dataModel = null
    roundingOptions = null
    borderOptions = null
    renderCommand = null
    currentBounds = null
  }
}
