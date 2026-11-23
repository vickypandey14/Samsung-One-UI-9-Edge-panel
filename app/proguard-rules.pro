# Debug builds are not minified. Keep the overlay service entry point if release shrinking is enabled later.
-keep class dev.edgepanel.app.overlay.service.EdgeOverlayService { *; }
