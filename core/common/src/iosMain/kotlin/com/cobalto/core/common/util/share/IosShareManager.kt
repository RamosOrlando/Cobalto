package com.cobalto.core.common.util.share

import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.Foundation.NSURL

class IosShareManager : ShareManager {
    override fun sharePdf(filePath: String, title: String) {
        val url = NSURL.fileURLWithPath(filePath)
        val activityViewController = UIActivityViewController(
            activityItems = listOf(url),
            applicationActivities = null
        )
        val window = UIApplication.sharedApplication.keyWindow
        window?.rootViewController?.presentViewController(
            viewControllerToPresent = activityViewController,
            animated = true,
            completion = null
        )
    }
}
