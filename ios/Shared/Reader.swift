import SwiftUI
import AVFoundation

func summarize(_ text: String) -> String {
    let sentences = text.components(separatedBy: .newlines).flatMap { line -> [String] in
        var result: [String] = []
        line.enumerateSubstrings(in: line.startIndex..<line.endIndex, options: .bySentences) { s, _, _, _ in
            if let s = s { result.append(s.trimmingCharacters(in: .whitespacesAndNewlines)) }
        }
        return result
    }.filter { !$0.isEmpty }
    if sentences.count <= 3 { return text }
    func words(_ s: String) -> [String] { s.lowercased().components(separatedBy: .alphanumerics.inverted).filter { $0.count > 3 } }
    var frequency: [String: Int] = [:]
    for s in sentences { for word in words(s) { frequency[word, default: 0] += 1 } }
    let scores = sentences.map { s -> Double in
        let tokens = words(s)
        return Double(tokens.reduce(0) { $0 + frequency[$1, default: 0] }) / sqrt(Double(max(1, tokens.count)))
    }
    let indices = sentences.indices.sorted { scores[$0] == scores[$1] ? $0 < $1 : scores[$0] > scores[$1] }.prefix(3).sorted()
    return indices.map { sentences[$0] }.joined(separator: "\n\n")
}

final class Reader: ObservableObject {
    @Published var text = ""
    @Published var output = ""
    private let speech = AVSpeechSynthesizer()
    func run(summary: Bool, aloud: Bool) {
        speech.stopSpeaking(at: .immediate)
        let source = text.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !source.isEmpty else { output = "Add or share some text first."; return }
        output = summary ? summarize(String(source.prefix(100000))) : source
        if aloud { speech.speak(AVSpeechUtterance(string: output)) }
    }
    func stop() { speech.stopSpeaking(at: .immediate) }
}

struct ReaderView: View {
    @ObservedObject var reader: Reader
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                Text("🦜 Parrot").font(.largeTitle.bold())
                Text("Too long? Do listen.").font(.title2)
                Text("Share text or a Safari page to Parrot, or paste text below. iOS does not allow a floating reader over other apps.")
                TextEditor(text: $reader.text).frame(minHeight: 150).overlay(RoundedRectangle(cornerRadius: 8).stroke(.secondary)).accessibilityLabel("Text to read")
                Button("Read aloud") { reader.run(summary: false, aloud: true) }
                Button("Summarize") { reader.run(summary: true, aloud: false) }
                Button("Summarize & read") { reader.run(summary: true, aloud: true) }
                Button("Stop") { reader.stop() }
                Text(reader.output).textSelection(.enabled)
                Text("Private by default. Text stays on this device. Summaries select three key sentences locally; they are not AI rewrites. Reading stops when you close this screen.").font(.footnote).foregroundStyle(.secondary)
            }.padding().buttonStyle(.bordered)
        }.onDisappear { reader.stop() }
    }
}
