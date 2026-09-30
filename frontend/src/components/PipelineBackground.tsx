// Decorative animation for the home page, centered at the bottom of whatever
// section wraps it (never behind the headline text). Tells one small story
// with icons: a user sends their CV to a company, and the company either
// makes an offer (a green check badge appears on the CV) or rejects it
// (a red cross badge appears on the CV) — both outcomes looping side by side
// since either one is possible. Pure SMIL/SVG, no JS, no extra dependencies.

// Both paths share the same first stretch (user -> company) and only
// diverge after the company node, so the CV visibly "arrives" there before
// the outcome is decided.
const OFFER_PATH = "M 40 100 C 100 100, 150 100, 210 100 C 320 100, 380 40, 620 40";
const REJECTED_PATH = "M 40 100 C 100 100, 150 100, 210 100 C 320 100, 380 160, 620 160";

function TravelingCv({ path, begin, badge }: { path: string; begin: string; badge: "offer" | "rejected" }) {
  return (
    // opacity starts at 0 and snaps to 1 the instant this CV's animateMotion
    // begins — without this, browsers render the group at its raw (0,0)
    // local position for the "begin" delay before the motion starts, which
    // shows up as a stray icon flashing near the top of the page on load.
    <g opacity="0" className="fill-surface stroke-brand" strokeWidth={1.6} strokeLinejoin="round">
      <animate attributeName="opacity" from="0" to="1" begin={begin} dur="10ms" fill="freeze" />

      {/* the CV itself: a small document with a few text lines */}
      <rect x="-7" y="-9.5" width="14" height="19" rx="1.8" />
      <line x1="-3.8" y1="-5" x2="3.8" y2="-5" strokeLinecap="round" />
      <line x1="-3.8" y1="-1.3" x2="3.8" y2="-1.3" strokeLinecap="round" />
      <line x1="-3.8" y1="2.4" x2="0.8" y2="2.4" strokeLinecap="round" />

      {/* outcome badge, attached to the CV — hidden until it reaches the end */}
      <g transform="translate(6.5, -8.5)" opacity="0">
        <circle r="5.4" className={badge === "offer" ? "fill-status-offer" : "fill-status-rejected"} />
        {badge === "offer" ? (
          <path
            d="M -2.4 0 L -0.7 1.7 L 2.7 -2"
            stroke="white"
            strokeWidth={1.4}
            fill="none"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        ) : (
          <g stroke="white" strokeWidth={1.4} strokeLinecap="round">
            <line x1="-2.2" y1="-2.2" x2="2.2" y2="2.2" />
            <line x1="2.2" y1="-2.2" x2="-2.2" y2="2.2" />
          </g>
        )}
        <animate attributeName="opacity" values="0;0;1;1" keyTimes="0;0.93;0.97;1" dur="6s" begin={begin} repeatCount="indefinite" />
      </g>

      <animateMotion path={path} dur="6s" begin={begin} repeatCount="indefinite" />
    </g>
  );
}

export default function PipelineBackground() {
  return (
    <div
      aria-hidden="true"
      className="pointer-events-none absolute inset-x-0 bottom-4 z-0 flex justify-center sm:bottom-6"
    >
      <svg viewBox="0 0 660 200" className="h-[135px] w-[94%] max-w-xl sm:h-[175px] sm:max-w-2xl">
        {/* the two paths */}
        <path d={OFFER_PATH} fill="none" strokeWidth={2} strokeLinecap="round" className="stroke-status-offer/30" />
        <path d={REJECTED_PATH} fill="none" strokeWidth={2} strokeLinecap="round" className="stroke-status-rejected/30" />

        {/* user node */}
        <g transform="translate(40, 100)">
          <circle r="20" className="fill-brand-tint" />
          <g className="stroke-brand" fill="none" strokeWidth={1.9} strokeLinecap="round" strokeLinejoin="round">
            <circle cx="0" cy="-4.5" r="5" />
            <path d="M -8.5 10.5 a 8.5 7.2 0 0 1 17 0" />
          </g>
        </g>

        {/* company node, sitting right where both paths meet before diverging */}
        <g transform="translate(210, 100)">
          <circle r="18" className="fill-surface stroke-line" strokeWidth={1.6} />
          <g className="stroke-ink-soft" fill="none" strokeWidth={1.6} strokeLinecap="round" strokeLinejoin="round">
            <rect x="-7.2" y="-3.5" width="14.4" height="10.2" rx="1.4" />
            <path d="M -3.8 -3.5 v-2.2 a 1.7 1.7 0 0 1 1.7 -1.7 h4.2 a 1.7 1.7 0 0 1 1.7 1.7 v2.2" />
          </g>
        </g>

        {/* offer / rejected outcome markers */}
        <g transform="translate(620, 40)">
          <circle r="15.5" className="fill-status-offer-bg" />
        </g>
        <g transform="translate(620, 160)">
          <circle r="15.5" className="fill-status-rejected-bg" />
        </g>

        {/* the two traveling CVs — staggered so both stories play out clearly */}
        <TravelingCv path={OFFER_PATH} begin="0s" badge="offer" />
        <TravelingCv path={REJECTED_PATH} begin="3s" badge="rejected" />
      </svg>
    </div>
  );
}