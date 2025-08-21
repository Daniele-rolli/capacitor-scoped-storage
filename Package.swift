// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "CapacitorScopedStorage",
    platforms: [.iOS(.v14)],
    products: [
        .library(
            name: "CapacitorScopedStorage",
            targets: ["CapacitorScopedStoragePlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "7.0.0")
    ],
    targets: [
        .target(
            name: "CapacitorScopedStoragePlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/CapacitorScopedStoragePlugin"),
        .testTarget(
            name: "CapacitorScopedStoragePluginTests",
            dependencies: ["CapacitorScopedStoragePlugin"],
            path: "ios/Tests/CapacitorScopedStoragePluginTests")
    ]
)