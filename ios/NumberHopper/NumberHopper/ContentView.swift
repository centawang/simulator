import SwiftUI

// MARK: - Fun Colors for Kids
extension Color {
    static let funBlue = Color(red: 0x42/255, green: 0xA5/255, blue: 0xF5/255)
    static let funGreen = Color(red: 0x66/255, green: 0xBB/255, blue: 0x6A/255)
    static let funOrange = Color(red: 0xFF/255, green: 0x70/255, blue: 0x43/255)
    static let funPink = Color(red: 0xEC/255, green: 0x40/255, blue: 0x7A/255)
    static let funPurple = Color(red: 0x7E/255, green: 0x57/255, blue: 0xC2/255)
    static let funYellow = Color(red: 0xFF/255, green: 0xEE/255, blue: 0x58/255)
    static let bgCream = Color(red: 0xFF/255, green: 0xF8/255, blue: 0xE1/255)
    static let brownAxis = Color(red: 0x8D/255, green: 0x6E/255, blue: 0x63/255)
}

let hopColors: [Color] = [.funOrange, .funGreen, .funBlue, .funPink]

// MARK: - ContentView
struct ContentView: View {
    @State private var firstNumber = ""
    @State private var secondNumber = ""
    @State private var editingFirst = true
    @State private var result: String? = nil
    @State private var addendA: Int? = nil
    @State private var addendB: Int? = nil
    @State private var animationStep = 0
    @State private var animationTimer: Timer? = nil

    var animationDone: Bool {
        guard let b = addendB else { return false }
        return animationStep > abs(b)
    }

    var bothValid: Bool {
        Int(firstNumber) != nil && Int(secondNumber) != nil
    }

    var body: some View {
        VStack(spacing: 0) {
            // Title
            Text("\u{1F522} Number Hopper!")
                .font(.system(size: 28, weight: .bold))
                .foregroundColor(.funPurple)
                .padding(.top, 16)

            // Answer card - always reserves space
            answerCard
                .padding(.top, 10)

            // Number line
            if let a = addendA, let b = addendB {
                numberLineSection(a: a, b: b)
                    .padding(.top, 10)
            } else {
                Spacer().frame(height: 190)
            }

            Spacer().frame(height: 12)

            // Number displays
            numberDisplays
            
            Spacer().frame(height: 12)

            // Number pad
            numberPad

            Spacer().frame(height: 12)

            // Add / Subtract buttons
            operationButtons

            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.bgCream)
    }

    // MARK: - Answer Card
    private var answerCard: some View {
        Group {
            if animationDone, let r = result {
                Text("Answer: \(r)")
                    .font(.system(size: 24, weight: .bold))
                    .foregroundColor(.funPurple)
                    .padding(.horizontal, 20)
                    .padding(.vertical, 12)
                    .background(Color.funYellow)
                    .clipShape(RoundedRectangle(cornerRadius: 20))
                    .shadow(radius: 2)
            } else {
                Text(" ")
                    .font(.system(size: 24, weight: .bold))
                    .padding(.horizontal, 20)
                    .padding(.vertical, 12)
                    .foregroundColor(.clear)
            }
        }
        .padding(.horizontal, 24)
    }

    // MARK: - Number Line Section
    private func numberLineSection(a: Int, b: Int) -> some View {
        let sum = a + b
        let lineMin = min(min(a, sum), 0) - 2
        let lineMax = max(max(a, sum), 0) + 2
        let rangeSpan = max(lineMax - lineMin, 5)
        let tickSpacing: CGFloat = 48
        let canvasWidth = tickSpacing * CGFloat(rangeSpan)

        return ScrollViewReader { proxy in
            ScrollView(.horizontal, showsIndicators: false) {
                NumberLineCanvas(
                    a: a, b: b, sum: sum,
                    lineMin: lineMin, lineMax: lineMax,
                    rangeSpan: rangeSpan,
                    animationStep: animationStep,
                    tickSpacing: tickSpacing
                )
                .frame(width: canvasWidth, height: 180)
                .id("numberLine")
            }
        }
    }

    // MARK: - Number Displays
    private var numberDisplays: some View {
        HStack(spacing: 12) {
            numberBox(
                label: "\u{1F535} First",
                value: firstNumber,
                isActive: editingFirst,
                activeColor: .funBlue
            ) {
                editingFirst = true
            }

            numberBox(
                label: "\u{1F7E2} Second",
                value: secondNumber,
                isActive: !editingFirst,
                activeColor: .funGreen
            ) {
                editingFirst = false
            }
        }
        .padding(.horizontal, 16)
    }

    private func numberBox(label: String, value: String, isActive: Bool, activeColor: Color, onTap: @escaping () -> Void) -> some View {
        VStack(spacing: 2) {
            Text(label)
                .font(.system(size: 12))
                .foregroundColor(.gray)
            Text(value.isEmpty ? "?" : value)
                .font(.system(size: 24, weight: .bold))
                .foregroundColor(value.isEmpty ? Color.gray.opacity(0.3) : activeColor)
        }
        .frame(maxWidth: .infinity)
        .frame(height: 64)
        .background(isActive ? Color.white : Color(white: 0.96))
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(isActive ? activeColor : .gray, lineWidth: isActive ? 3 : 1)
        )
        .onTapGesture(perform: onTap)
    }

    // MARK: - Number Pad
    private var numberPad: some View {
        let digitRows = [["1","2","3"], ["4","5","6"], ["7","8","9"]]
        return VStack(spacing: 6) {
            ForEach(digitRows, id: \.self) { row in
                HStack(spacing: 8) {
                    ForEach(row, id: \.self) { digit in
                        digitButton(digit)
                    }
                }
            }
            // Bottom row: Delete, 0
            HStack(spacing: 8) {
                Button(action: onDelete) {
                    Text("\u{232B}")
                        .font(.system(size: 22, weight: .bold))
                        .frame(maxWidth: .infinity)
                        .frame(height: 52)
                        .background(Color.funOrange)
                        .foregroundColor(.white)
                        .clipShape(RoundedRectangle(cornerRadius: 14))
                }
                digitButton("0")
            }
        }
        .padding(.horizontal, 16)
    }

    private func digitButton(_ digit: String) -> some View {
        Button(action: { onDigit(digit) }) {
            Text(digit)
                .font(.system(size: 24, weight: .bold))
                .foregroundColor(.funPurple)
                .frame(maxWidth: .infinity)
                .frame(height: 52)
                .background(Color.white)
                .clipShape(RoundedRectangle(cornerRadius: 14))
                .shadow(color: .black.opacity(0.05), radius: 1, y: 1)
        }
    }

    // MARK: - Operation Buttons
    private var operationButtons: some View {
        HStack(spacing: 16) {
            Button(action: onAdd) {
                Text("\u{2795} Add!")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(.white)
                    .padding(.horizontal, 24)
                    .frame(height: 50)
                    .background(bothValid ? Color.funGreen : Color.gray)
                    .clipShape(RoundedRectangle(cornerRadius: 16))
            }
            .disabled(!bothValid)

            Button(action: onSubtract) {
                Text("\u{2796} Subtract!")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(.white)
                    .padding(.horizontal, 24)
                    .frame(height: 50)
                    .background(bothValid ? Color.funOrange : Color.gray)
                    .clipShape(RoundedRectangle(cornerRadius: 16))
            }
            .disabled(!bothValid)
        }
    }

    // MARK: - Actions
    private func onDigit(_ digit: String) {
        if editingFirst {
            if firstNumber.count < 6 { firstNumber += digit }
        } else {
            if secondNumber.count < 6 { secondNumber += digit }
        }
    }

    private func onDelete() {
        if editingFirst {
            if !firstNumber.isEmpty { firstNumber.removeLast() }
        } else {
            if !secondNumber.isEmpty { secondNumber.removeLast() }
        }
    }

    private func onAdd() {
        guard let a = Int(firstNumber), let b = Int(secondNumber) else { return }
        startAnimation(a: a, b: b, res: a + b)
    }

    private func onSubtract() {
        guard let a = Int(firstNumber), let b = Int(secondNumber) else { return }
        startAnimation(a: a, b: -b, res: a - b)
    }

    private func startAnimation(a: Int, b: Int, res: Int) {
        animationTimer?.invalidate()
        animationStep = 0
        addendA = a
        addendB = b
        result = "\(res)"
        editingFirst = true

        let totalSteps = abs(b)
        let baseDelay = max(0.3, 1.5 / Double(max(totalSteps, 1)))

        var currentStep = 0
        animationTimer = Timer.scheduledTimer(withTimeInterval: baseDelay, repeats: true) { timer in
            currentStep += 1
            animationStep = currentStep
            if currentStep > totalSteps + 1 {
                timer.invalidate()
            }
        }
    }
}

// MARK: - Number Line Canvas
struct NumberLineCanvas: View {
    let a: Int
    let b: Int
    let sum: Int
    let lineMin: Int
    let lineMax: Int
    let rangeSpan: Int
    let animationStep: Int
    let tickSpacing: CGFloat

    var body: some View {
        Canvas { context, size in
            let w = size.width
            let h = size.height
            let baseY = h * 0.7
            let pxPerUnit = w / CGFloat(rangeSpan)

            func valueToX(_ v: Int) -> CGFloat {
                CGFloat(v - lineMin) * pxPerUnit
            }

            // Main axis line
            let axisPath = Path { p in
                p.move(to: CGPoint(x: 0, y: baseY))
                p.addLine(to: CGPoint(x: w, y: baseY))
            }
            context.stroke(axisPath, with: .color(.brownAxis), lineWidth: 4)

            // Tick marks and labels
            for tick in lineMin...lineMax {
                let x = valueToX(tick)
                let isKeyTick = tick == a || tick == sum || tick == 0
                let tickH: CGFloat = isKeyTick ? 24 : 14
                let tickColor: Color = tick == a ? .funBlue : (tick == sum ? .funPink : .brownAxis)
                let tickW: CGFloat = isKeyTick ? 4 : 2

                let tickPath = Path { p in
                    p.move(to: CGPoint(x: x, y: baseY - tickH))
                    p.addLine(to: CGPoint(x: x, y: baseY + tickH))
                }
                context.stroke(tickPath, with: .color(tickColor), lineWidth: tickW)

                // Label
                let fontSize: CGFloat = isKeyTick ? 18 : 14
                let text = Text("\(tick)")
                    .font(.system(size: fontSize, weight: isKeyTick ? .bold : .regular))
                    .foregroundColor(tickColor)
                let resolved = context.resolve(text)
                let textSize = resolved.measure(in: CGSize(width: 100, height: 50))
                context.draw(resolved, at: CGPoint(x: x, y: baseY + tickH + textSize.height / 2 + 4))
            }

            // Blue dot at starting value
            let startDot = Path(ellipseIn: CGRect(x: valueToX(a) - 16, y: baseY - 16, width: 32, height: 32))
            context.fill(startDot, with: .color(.funBlue))

            let absB = abs(b)
            let direction = b > 0 ? 1 : -1
            let stepsShown = min(animationStep, absB)
            let showSummary = animationStep > absB

            // Progressive pink dot
            let currentPos = a + direction * stepsShown
            let dotPos = showSummary ? sum : currentPos
            let endDot = Path(ellipseIn: CGRect(x: valueToX(dotPos) - 16, y: baseY - 16, width: 32, height: 32))
            context.fill(endDot, with: .color(.funPink))

            guard b != 0 else { return }

            let unitArcHeight = pxPerUnit * 0.4

            // Individual +1 / -1 arcs
            for i in 0..<stepsShown {
                let arcColor = hopColors[i % hopColors.count]
                let arcStart = a + direction * i
                let arcEnd = arcStart + direction
                let sx = valueToX(arcStart)
                let ex = valueToX(arcEnd)

                // Arc path
                let arcPath = Path { p in
                    p.move(to: CGPoint(x: sx, y: baseY))
                    p.addCurve(
                        to: CGPoint(x: ex, y: baseY),
                        control1: CGPoint(x: sx, y: baseY - unitArcHeight),
                        control2: CGPoint(x: ex, y: baseY - unitArcHeight)
                    )
                }
                context.stroke(arcPath, with: .color(arcColor), lineWidth: 3)

                // Arrowhead
                let arrowSize: CGFloat = 10
                let arrowDir: CGFloat = b > 0 ? -1 : 1
                let arr1 = Path { p in
                    p.move(to: CGPoint(x: ex, y: baseY))
                    p.addLine(to: CGPoint(x: ex + arrowDir * arrowSize, y: baseY - arrowSize))
                }
                let arr2 = Path { p in
                    p.move(to: CGPoint(x: ex, y: baseY))
                    p.addLine(to: CGPoint(x: ex + arrowDir * arrowSize, y: baseY + arrowSize * 0.3))
                }
                context.stroke(arr1, with: .color(arcColor), lineWidth: 3)
                context.stroke(arr2, with: .color(arcColor), lineWidth: 3)

                // +1 / -1 label
                let unitLabel = b > 0 ? "+1" : "\u{2212}1"
                let labelText = Text(unitLabel)
                    .font(.system(size: 12, weight: .bold))
                    .foregroundColor(arcColor)
                let resolvedLabel = context.resolve(labelText)
                context.draw(resolvedLabel, at: CGPoint(x: (sx + ex) / 2, y: baseY - unitArcHeight - 6))
            }

            // Summary arc
            if showSummary {
                let startX = valueToX(a)
                let endX = valueToX(sum)
                let arcHeight = min(CGFloat(absB) * pxPerUnit * 0.4, h * 0.5).clamped(to: pxPerUnit * 0.6...10000)
                let midX = (startX + endX) / 2

                let summaryPath = Path { p in
                    p.move(to: CGPoint(x: startX, y: baseY))
                    p.addCurve(
                        to: CGPoint(x: endX, y: baseY),
                        control1: CGPoint(x: startX, y: baseY - arcHeight),
                        control2: CGPoint(x: endX, y: baseY - arcHeight)
                    )
                }
                context.stroke(summaryPath, with: .color(.funPurple), lineWidth: 4)

                // Arrowhead
                let arrowSize: CGFloat = 14
                let arrowDir: CGFloat = b > 0 ? -1 : 1
                let sa1 = Path { p in
                    p.move(to: CGPoint(x: endX, y: baseY))
                    p.addLine(to: CGPoint(x: endX + arrowDir * arrowSize, y: baseY - arrowSize))
                }
                let sa2 = Path { p in
                    p.move(to: CGPoint(x: endX, y: baseY))
                    p.addLine(to: CGPoint(x: endX + arrowDir * arrowSize, y: baseY + arrowSize * 0.3))
                }
                context.stroke(sa1, with: .color(.funPurple), lineWidth: 4)
                context.stroke(sa2, with: .color(.funPurple), lineWidth: 4)

                // Summary label
                let displayB = abs(b)
                let label = b > 0 ? "+\(displayB)" : "\u{2212}\(displayB)"
                let summaryText = Text(label)
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(.funPurple)
                let resolvedSummary = context.resolve(summaryText)
                context.draw(resolvedSummary, at: CGPoint(x: midX, y: baseY - arcHeight - 8))
            }
        }
    }
}

// MARK: - Comparable clamped helper
extension Comparable {
    func clamped(to range: ClosedRange<Self>) -> Self {
        min(max(self, range.lowerBound), range.upperBound)
    }
}
