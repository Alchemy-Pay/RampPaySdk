// swift-tools-version:5.3
import PackageDescription

let package = Package(
    name: "RampPaySdk",
    platforms: [
        .iOS(.v11) // 设置你支持的最低 iOS 版本
    ],
    products: [
        .library(
            name: "RampPaySdk",
            targets: ["RampPaySdk"]
        ),
    ],
    dependencies: [
        // 在此添加依赖项，如果有的话
    ],
    targets: [
        .binaryTarget(
            name: "RampPaySdk",
             path: "./RampPaySdk.xcframework"
        )
    ]
)
