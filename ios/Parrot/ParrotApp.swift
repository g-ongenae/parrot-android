import SwiftUI
@main struct ParrotApp: App {
    @StateObject private var reader = Reader()
    var body: some Scene { WindowGroup { ReaderView(reader: reader) } }
}
