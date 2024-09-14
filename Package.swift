import PackageDescription

let package = Package(
    name: "RampPaySdk",
    platforms: [
        .iOS(.v14) // 设置你支持的最低 iOS 版本
    ],
    products: [
        .library(
            name: "RampPaySdk",
            targets: ["RampPaySdk"]
        ),
    ],
    targets: [
        .binaryTarget(
            name: "RampPaySdk",
            path: "./RampPaySdk.xcframework"
        )
    ]
)
